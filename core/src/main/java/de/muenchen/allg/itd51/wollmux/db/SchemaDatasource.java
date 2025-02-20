/*-
 * #%L
 * WollMux
 * %%
 * Copyright (C) 2005 - 2022 Landeshauptstadt München
 * %%
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * #L%
 */
package de.muenchen.allg.itd51.wollmux.db;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.muenchen.allg.itd51.wollmux.config.ConfigThingy;
import de.muenchen.allg.itd51.wollmux.config.ConfigurationErrorException;
import de.muenchen.allg.itd51.wollmux.config.NodeNotFoundException;
import de.muenchen.allg.itd51.wollmux.util.L;

/**
 * Datenquelle, die die Daten einer existierenden Datenquelle mit geänderten Spalten
 * zur Verfügung stellt.
 * 
 * @author Matthias Benkmann (D-III-ITD 5.1)
 */
public class SchemaDatasource extends Datasource
{

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaDatasource.class);

  private static final Pattern SPALTENNAME = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");

  private static final String EMPTY_COLUMN = "";

  private Datasource source;

  private String sourceName;

  private String name;

  private List<String> schema;

  private Map<String, String> mapNewToOld;

  /**
   * Erzeugt eine neue SchemaDatasource.
   * 
   * @param nameToDatasource
   *          enthält alle bis zum Zeitpunkt der Definition dieser SchemaDatasource
   *          bereits vollständig instanziierten Datenquellen.
   * @param sourceDesc
   *          der "Datenquelle"-Knoten, der die Beschreibung dieser SchemaDatasource
   *          enthält.
   * @param context
   *          der Kontext relativ zu dem URLs aufgelöst werden sollen (zur Zeit nicht
   *          verwendet).
   */
  public SchemaDatasource(Map<String, Datasource> nameToDatasource,
      ConfigThingy sourceDesc, URL context)
  {
    name = parseConfig(sourceDesc, "NAME", () -> L.m("NAME der Datenquelle fehlt"));
    sourceName = parseConfig(sourceDesc, "SOURCE", () -> L.m("SOURCE der Datenquelle %1 fehlt", name));

    source = nameToDatasource.get(sourceName);

    if (source == null)
      throw new ConfigurationErrorException(L.m("Fehler bei Initialisierung von Datenquelle \"%1\": "
          + "Referenzierte Datenquelle \"%2\" nicht (oder fehlerhaft) definiert", name, sourceName));

    schema = new ArrayList<>(source.getSchema());
    mapNewToOld = new HashMap<>();

    List<String> columnsToDrop = dropColumns(sourceDesc.query("DROP"));
    List<String> columnsToAdd = addColumns(sourceDesc.query("ADD"), columnsToDrop);
    renameColumn(sourceDesc.query("RENAME"), columnsToDrop, columnsToAdd);

    /**
     * Für alle hinzugefügten Spalten, die weder in der Originaldatenbank existieren
     * noch durch einen RENAME auf eine Spalte der Originaldatenbank abgebildet
     * werden, füge ein Pseudomapping auf EMPTY_COLUMN hinzu, damit
     * RenameDataset.get() weiss, dass es für die Spalte null liefern soll.
     */
    for (String spalte : columnsToAdd)
    {
      if (!schema.contains(spalte) && !mapNewToOld.containsKey(spalte))
        mapNewToOld.put(spalte, EMPTY_COLUMN);
    }

    schema.removeAll(columnsToDrop);
    schema.addAll(columnsToAdd);
  }

  private void renameColumn(ConfigThingy renamesDesc, List<String> columnsToDrop, List<String> columnsToAdd)
  {
    for (ConfigThingy renameDesc : renamesDesc)
    {
      if (renameDesc.count() != 2)
        throw new ConfigurationErrorException(L.m(
          "Fehlerhafte RENAME Angabe in Datenquelle \"%1\"", name));

      String spalte1 = "";
      String spalte2 = "";
      try
      {
        spalte1 = renameDesc.getFirstChild().toString();
        spalte2 = renameDesc.getLastChild().toString();
      }
      catch (NodeNotFoundException x)
      {
        LOGGER.trace("", x);
      }

      if (!schema.contains(spalte1))
        throw new ConfigurationErrorException(L.m(
          "Spalte \"%1\" ist nicht im Schema", spalte1));

      if (!SPALTENNAME.matcher(spalte2).matches())
        throw new ConfigurationErrorException(L.m(
          "\"%2\" ist kein erlaubter Spaltenname", spalte2));

      mapNewToOld.put(spalte2, spalte1);
      columnsToDrop.add(spalte1);
      columnsToDrop.remove(spalte2);
      columnsToAdd.add(spalte2);
    }
  }

  private List<String> addColumns(ConfigThingy adds, List<String> columnsToDrop)
  {
    List<String> columnsToAdd = new ArrayList<>();
    for (ConfigThingy add : adds)
    {
      for (ConfigThingy addColumn : add)
      {
        String spalte = addColumn.toString();
        if (!SPALTENNAME.matcher(spalte).matches())
          throw new ConfigurationErrorException(L.m(
            "\"%1\" ist kein erlaubter Spaltenname", spalte));
        columnsToAdd.add(spalte);
        columnsToDrop.remove(spalte);
      }
    }
    return columnsToAdd;
  }

  private List<String> dropColumns(ConfigThingy drops)
  {
    List<String> columnsToDrop = new ArrayList<>();
    for (ConfigThingy drop : drops)
    {
      for (ConfigThingy dropColumn : drop)
      {
        String spalte = dropColumn.toString();
        if (!schema.contains(spalte))
          throw new ConfigurationErrorException(L.m(
            "Spalte \"%1\" ist nicht im Schema", spalte));
        columnsToDrop.add(spalte);
      }
    }
    return columnsToDrop;
  }

  @Override
  public List<String> getSchema()
  {
    return schema;
  }

  @Override
  public QueryResults getDatasetsByKey(Collection<String> keys)
  {
    return wrapDatasets(source.getDatasetsByKey(keys));
  }

  @Override
  public QueryResults getContents()
  {
    return new QueryResultsList(new Vector<RenameDataset>(0));
  }

  @Override
  public QueryResults find(List<QueryPart> query)
  {
    List<QueryPart> translatedQuery = new ArrayList<>(query.size());
    Iterator<QueryPart> iter = query.iterator();
    while (iter.hasNext())
    {
      QueryPart p = iter.next();
      String spalte = p.getColumnName();

      if (!schema.contains(spalte)) // dieser Test ist nicht redundant wegen DROPs
        return new QueryResultsList(new Vector<RenameDataset>(0));

      String alteSpalte = mapNewToOld.get(spalte);

      if (alteSpalte == /* nicht equals()!!!! */EMPTY_COLUMN)
        return new QueryResultsList(new Vector<RenameDataset>(0));

      if (alteSpalte != null)
        translatedQuery.add(new QueryPart(alteSpalte, p.getSearchString()));
      else
        translatedQuery.add(p);
    }
    return wrapDatasets(source.find(translatedQuery));
  }

  @Override
  public String getName()
  {
    return name;
  }

  private QueryResults wrapDatasets(QueryResults res)
  {
    List<RenameDataset> wrappedRes = new ArrayList<>(res.size());
    Iterator<Dataset> iter = res.iterator();
    while (iter.hasNext())
      wrappedRes.add(new RenameDataset(iter.next()));

    return new QueryResultsList(wrappedRes);
  }

  private class RenameDataset implements Dataset
  {
    private Dataset ds;

    public RenameDataset(Dataset ds)
    {
      this.ds = ds;
    }

    @Override
    public String get(String columnName) throws ColumnNotFoundException
    {
      // dieser Test ist nicht redundant wegen DROPs
      if (!schema.contains(columnName))
        throw new ColumnNotFoundException(L.m("Spalte \"%1\" existiert nicht!",
          columnName));

      String alteSpalte = mapNewToOld.get(columnName);

      if (alteSpalte == /* nicht equals()!!!! */EMPTY_COLUMN) {
        return null;
      }

      if (alteSpalte != null)
        return ds.get(alteSpalte);
      else
        return ds.get(columnName);
    }

    @Override
    public String getKey()
    {
      return ds.getKey();
    }
  }

}
