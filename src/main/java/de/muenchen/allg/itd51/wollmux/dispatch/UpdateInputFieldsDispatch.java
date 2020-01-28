package de.muenchen.allg.itd51.wollmux.dispatch;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchResultListener;
import com.sun.star.frame.XFrame;
import com.sun.star.util.URL;

import de.muenchen.allg.afid.UNO;
import de.muenchen.allg.itd51.wollmux.event.WollMuxEventHandler;

/**
 * Dispatch for updating input fields.
 */
public class UpdateInputFieldsDispatch extends WollMuxNotifyingDispatch
{
  /**
   * Command of this dispatch.
   */
  public static final String COMMAND = ".uno:UpdateInputFields";

  UpdateInputFieldsDispatch(XDispatch origDisp, URL origUrl, XFrame frame)
  {
    super(origDisp, origUrl, frame);
  }

  @Override
  public void dispatchWithNotification(URL url, PropertyValue[] props,
      XDispatchResultListener listener)
  {
    this.props = props;
    this.listener = listener;
    WollMuxEventHandler.getInstance().handleUpdateInputFields(
        UNO.XTextDocument(frame.getController().getModel()), this, isSynchronMode(props));
  }

  @Override
  public void dispatch(URL url, PropertyValue[] props)
  {
    this.props = props;
    WollMuxEventHandler.getInstance().handleUpdateInputFields(
        UNO.XTextDocument(frame.getController().getModel()), this, isSynchronMode(props));
  }

}
