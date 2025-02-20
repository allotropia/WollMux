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
package de.muenchen.allg.itd51.wollmux.func;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import de.muenchen.allg.itd51.wollmux.config.ConfigThingy;
import de.muenchen.allg.itd51.wollmux.config.ConfigurationErrorException;
import de.muenchen.allg.itd51.wollmux.dialog.DialogLibrary;
import de.muenchen.allg.itd51.wollmux.util.L;

public abstract class MultiFunction implements Function
{
  protected Collection<Function> subFunction;

  private String[] params;

  public MultiFunction(ConfigThingy conf, FunctionLibrary funcLib,
      DialogLibrary dialogLib, Map<Object, Object> context)
  {
    List<Function> subFunc = new ArrayList<>(conf.count());
    Iterator<ConfigThingy> iter = conf.iterator();
    while (iter.hasNext())
    {
      ConfigThingy subFunConf = iter.next();
      if (handleParam(subFunConf, funcLib, dialogLib, context)) continue;
      Function fun = FunctionFactory.parse(subFunConf, funcLib, dialogLib, context);
      subFunc.add(fun);
    }

    if (subFunc.isEmpty())
      throw new ConfigurationErrorException(L.m(
        "Funktion %1 erfordert mindestens einen Parameter", conf.getName()));

    init(subFunc);
  }

  /**
   * Liefert true, wenn conf von handleParam bereits vollständig behandelt wurde
   * und nicht mehr als Subfunktion erfasst werden soll. Diese Funktion wird von
   * Unterklassen überschrieben, um spezielle Parameter zu behandeln. ACHTUNG! Wird
   * diese Methode überschrieben, so sind normalerweise auch
   * {@link #getAdditionalParams()} und
   * {@link #getFunctionDialogReferences(Collection)} zu überschreiben, um die
   * zusätzlichen Funktionen aus dem behandelten Parameter zu behandeln.
   */
  @SuppressWarnings("squid:S1172")
  protected boolean handleParam(ConfigThingy conf, FunctionLibrary funcLib,
      DialogLibrary dialogLib, Map<Object, Object> context)
  {
    return false;
  }

  /**
   * Liefert die Namen der Parameter der zusätzlichen Funktionen, die von
   * {@link #handleParam(ConfigThingy, FunctionLibrary, DialogLibrary, Map)}
   * geparst wurden oder null, falls es keine gibt.
   */
  protected String[] getAdditionalParams()
  {
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  public MultiFunction(Collection<Function> subFunction)
  {
    init(subFunction);
  }

  private void init(Collection<Function> subFunction)
  {
    this.subFunction = subFunction;

    // Ein Set wäre performanter, aber so wird die Reihenfolge beibehalten. Evtl.
    // macht es Sinn, auch die anderen Function-Klassen, die im Moment Sets
    // verwenden auf das Konstrukt mit Liste und contains()-Test umzustellen und
    // dadurch eine wohldefinierte Reihenfolge zu erreichen. Derzeit ist der Bedarf
    // dafür aber nicht gegeben, insbes. da insertFunctionValue umgeschrieben
    // werden soll und der FM4000 bereits umgeschrieben wurde um nicht von der
    // Reihenfolge abzuhängen.
    ArrayList<String> deps = new ArrayList<>();
    for (Function f : subFunction)
    {
      String[] parameter = f.parameters();
      for (String str : parameter)
      {
        if (!deps.contains(str)) deps.add(str);
      }
    }

    for (String str : getAdditionalParams())
    {
      if (!deps.contains(str))
      {
        deps.add(str);
      }
    }

    params = new String[deps.size()];
    params = deps.toArray(params);
  }

  @Override
  public boolean getBoolean(Values parameters)
  {
    return getResult(parameters).equalsIgnoreCase("true");
  }

  @Override
  public String[] parameters()
  {
    return params;
  }

  @Override
  public void getFunctionDialogReferences(Collection<String> set)
  {
    Iterator<Function> iter = subFunction.iterator();
    while (iter.hasNext())
    {
      iter.next().getFunctionDialogReferences(set);
    }
  }
}
