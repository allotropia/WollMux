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
package de.muenchen.allg.itd51.wollmux.former.section;

import java.util.Iterator;

import de.muenchen.allg.itd51.wollmux.former.BroadcastListener;
import de.muenchen.allg.itd51.wollmux.former.BroadcastObjectSelection;
import de.muenchen.allg.itd51.wollmux.former.FormularMax4kController;
import de.muenchen.allg.itd51.wollmux.former.group.model.GroupModelList;
import de.muenchen.allg.itd51.wollmux.former.section.model.SectionModel;
import de.muenchen.allg.itd51.wollmux.former.section.model.SectionModelList;
import de.muenchen.allg.itd51.wollmux.former.view.OnDemandCardView;
import de.muenchen.allg.itd51.wollmux.former.view.View;
import de.muenchen.allg.itd51.wollmux.former.view.ViewChangeListener;
import de.muenchen.allg.itd51.wollmux.util.L;

/**
 * Eine View, die alle
 * {@link de.muenchen.allg.itd51.wollmux.former.section.OneSectionExtView}s enthält.
 * 
 * @author Matthias Benkmann (D-III-ITD 5.1)
 */
public class AllSectionExtViewsPanel extends OnDemandCardView
{
  /**
   * Diese Sichtbarkeitsgruppen stehen zur Auswahl für das GROUPS-Attribut.
   */
  private GroupModelList groupModelList;

  /**
   * Erzeugt ein {@link AllSectionExtViewsPanel}, das den Inhalt von
   * sectionModelList anzeigt.
   * 
   * @param groupModelList
   *          die Liste der Sichtbarkeitsgruppen die zur Auswahl angeboten werden
   *          sollen.
   */
  public AllSectionExtViewsPanel(SectionModelList sectionModelList,
      GroupModelList groupModelList, FormularMax4kController formularMax4000)
  {
    super(L.m("GROUPS-View"));
    this.groupModelList = groupModelList;
    sectionModelList.addListener(new MyItemListener());
    formularMax4000.addBroadcastListener(new MyBroadcastListener());

    Iterator<SectionModel> iter = sectionModelList.iterator();
    while (iter.hasNext())
    {
      SectionModel model = iter.next();
      if (model.hasGroups()) addItem(model);
    }
  }

  @Override
  public View createViewFor(Object model, ViewChangeListener viewChangeListener)
  {
    SectionModel m = (SectionModel) model;
    return new OneSectionExtView(m, groupModelList, viewChangeListener);
  }

  private class MyItemListener implements SectionModelList.ItemListener
  {
    @Override
    public void itemAdded(SectionModel model, int index)
    {
      if (model.hasGroups()) addItem(model);
    }

    @Override
    public void itemRemoved(SectionModel model, int index)
    {
    // Hier muss nicht removeItem(model) aufgerufen werden. Dies behandelt die
    // OnDemandCardView selbst mittels Listener
    }
  }

  private class MyBroadcastListener implements BroadcastListener
  {
    @Override
    public void broadcastFormControlModelSelection(BroadcastObjectSelection b)
    {
      showEmpty();
    }

    @Override
    public void broadcastSectionModelSelection(BroadcastObjectSelection b)
    {
      if (b.getState() == 1)
      {
        show(b.getObject());
      }
      else
      {
        showEmpty();
      }
    }
  }

}
