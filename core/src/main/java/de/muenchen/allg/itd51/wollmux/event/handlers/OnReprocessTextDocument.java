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
package de.muenchen.allg.itd51.wollmux.event.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.muenchen.allg.itd51.wollmux.WollMuxFehlerException;
import de.muenchen.allg.itd51.wollmux.WollMuxFiles;
import de.muenchen.allg.itd51.wollmux.document.TextDocumentController;
import de.muenchen.allg.itd51.wollmux.document.TextDocumentModel;
import de.muenchen.allg.itd51.wollmux.document.WMCommandsFailedException;
import de.muenchen.allg.itd51.wollmux.document.commands.DocumentCommandInterpreter;

/**
 * Event for reprocessing a document. Only new commands are executed.
 */
public class OnReprocessTextDocument extends WollMuxEvent
{

  private static final Logger LOGGER = LoggerFactory.getLogger(OnReprocessTextDocument.class);

  TextDocumentModel model;
  private TextDocumentController documentController;

  /**
   * Create this event.
   *
   * @param documentController
   *          The document.
   */
  public OnReprocessTextDocument(TextDocumentController documentController)
  {
    this.documentController = documentController;
    this.model = documentController.getModel();
  }

  @Override
  protected void doit() throws WollMuxFehlerException
  {
    if (model == null)
    {
      return;
    }

    model.getDocumentCommands().update();
    DocumentCommandInterpreter dci = new DocumentCommandInterpreter(
        documentController, WollMuxFiles.isDebugMode());

    try
    {
      dci.executeTemplateCommands();
      dci.scanGlobalDocumentCommands();
      dci.scanInsertFormValueCommands();
    } catch (WMCommandsFailedException e)
    {
      LOGGER.debug("", e);
    }
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName() + "(" + model + ")";
  }
}
