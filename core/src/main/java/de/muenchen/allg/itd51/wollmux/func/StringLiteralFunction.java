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

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

public class StringLiteralFunction implements Function
{
  private String literal;

  private boolean bool;

  @Override
  public String[] parameters()
  {
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  @Override
  public void getFunctionDialogReferences(Collection<String> set)
  {
    // this function can't have any function dialog references
  }

  public StringLiteralFunction(String str)
  {
    literal = str;
    bool = literal.equalsIgnoreCase("true");
  }

  @Override
  public String getResult(Values parameters)
  {
    return literal;
  }

  @Override
  public boolean getBoolean(Values parameters)
  {
    return bool;
  }
}
