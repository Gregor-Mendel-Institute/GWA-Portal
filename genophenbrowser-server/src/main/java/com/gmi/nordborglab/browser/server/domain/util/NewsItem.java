package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.06.13
 * Time: 12:48
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "news", schema = "util")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "util.news_id_seq", allocationSize = 1)
public class NewsItem extends BaseEntity {

    private String title;
    private String content;
    private String type;

    @Transient
    private boolean isRead = false;

    @Column(name = "create_date")
    private Date createDate;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private AppUser author;


    public NewsItem() {
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public AppUser getAuthor() {
        return author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setAuthor(AppUser author) {
        this.author = author;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isRead(Date lastCheckDate) {
        if (lastCheckDate != null && lastCheckDate.getTime() > createDate.getTime()) {
            isRead = true;
        } else {
            isRead = false;
        }
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
