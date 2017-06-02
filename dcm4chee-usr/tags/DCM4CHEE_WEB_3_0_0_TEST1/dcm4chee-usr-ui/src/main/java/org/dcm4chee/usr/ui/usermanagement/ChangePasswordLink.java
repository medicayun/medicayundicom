package org.dcm4chee.usr.ui.usermanagement;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.dcm4chee.usr.entity.User;

public class ChangePasswordLink extends AjaxFallbackLink<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private ModalWindow window;
    private String userId;
    private User forUser;
    
    public ChangePasswordLink(String id, ModalWindow window, String userId, final User forUser) {
        super(id);
        
        this.window = window;
        this.userId = userId;
        this.forUser = forUser;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        window
            .setCloseButtonCallback(
                    new ModalWindow.CloseButtonCallback() {
    
                        private static final long serialVersionUID = 1L;
                        
                        public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                            return true;
                        }
                    }
            ).setPageCreator(
                    new ModalWindow.PageCreator() {
    
                        private static final long serialVersionUID = 1L;
                        
                        @Override
                        public Page createPage() {
                            return new ChangePasswordPage(userId, forUser, window);
                        }
                    }
            ).show(target);
    }
}
