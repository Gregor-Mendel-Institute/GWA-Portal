package com.gmi.nordborglab.browser.server.domain;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/28/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */

@MappedSuperclass
public abstract class SecureEntity extends BaseEntity {

    @Transient
    private boolean isPublic = false;
    @Transient
    private CustomAccessControlEntry userPermission = null;
    @Transient
    private boolean isOwner = false;

    @Transient
    private AppUser owner;

    public void setIsPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void setUserPermission(CustomAccessControlEntry userPermission) {
        if (this.userPermission == null || this.userPermission.getMask() < userPermission.getMask()) {
            this.userPermission = userPermission;
        }
    }

    public CustomAccessControlEntry getUserPermission() {
        return userPermission;
    }

    public void setIsOwner(boolean owner) {
        this.isOwner = owner;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwnerUser(AppUser owner) {
        this.owner = owner;
    }

    public AppUser getOwnerUser() {
        return owner;
    }


    public abstract String getIndexType();

    public abstract String getRouting();
}
