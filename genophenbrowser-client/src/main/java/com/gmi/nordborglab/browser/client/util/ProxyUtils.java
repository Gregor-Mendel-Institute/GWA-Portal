package com.gmi.nordborglab.browser.client.util;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.AutoBeanVisitor;
import com.google.web.bindery.requestfactory.shared.BaseProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import com.google.web.bindery.requestfactory.shared.impl.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by uemit.seren on 1/23/15.
 */
public class ProxyUtils {

    // http://google-web-toolkit.googlecode.com/svn-history/r9024/trunk/user/src/com/google/gwt/requestfactory/client/impl/EntityProxyCategory.java
    static final String STABLE_ID = "stableId";

    // http://stackoverflow.com/questions/12462174/requestfactory-clone-proxy-to-new-context/20180605#20180605
    public static <T extends BaseProxy> AutoBean<T> cloneBeanProperties(final T original, final T clone, final RequestContext toContext) {

        AutoBean<T> originalBean = AutoBeanUtils.getAutoBean(original);
        AutoBean<T> cloneBean = AutoBeanUtils.getAutoBean(clone);

        return cloneBeanProperties(originalBean, cloneBean, toContext);
    }

    /**
     * Shallow-clones an autobean and makes duplicates of the collection types.
     * A regular {@link AutoBean#clone} won't duplicate reference properties.
     */
    public static <T extends BaseProxy> AutoBean<T> cloneBeanProperties(final AutoBean<T> toClone, final AutoBean<T> clone, final RequestContext toContext) {
        // NOTE: This may be a bad idea, i don't know if the STABLE_ID is the type or if it is the
        // actual server side reference to this object. Don't want it to update my original object.
        // I added this back in because I was getting an InstantionException in the locator I am
        // pretty sure this is because request factory server side could not resolve the class type.
        // Maybe someone could shed some light on this one, if you know what the stable id is really
        // used for.
        clone.setTag(STABLE_ID, toClone.getTag(STABLE_ID));
        clone.setTag(Constants.VERSION_PROPERTY_B64, toClone.getTag(Constants.VERSION_PROPERTY_B64));
        clone.accept(new AutoBeanVisitor() {
            final Map<String, Object> values = AutoBeanUtils.getAllProperties(toClone);

            @Override
            public boolean visitCollectionProperty(String propertyName, AutoBean<Collection<?>> value, CollectionPropertyContext ctx) {
                // javac generics bug
                value = AutoBeanUtils.<Collection<?>, Collection<?>>getAutoBean((Collection<?>) values.get(propertyName));
                if (value != null) {
                    Collection<Object> collection;
                    if (List.class == ctx.getType()) {
                        collection = new ArrayList<Object>();
                    } else if (Set.class == ctx.getType()) {
                        collection = new HashSet<Object>();
                    } else {
                        // Should not get here if the validator works correctly
                        throw new IllegalArgumentException(ctx.getType().getName());
                    }

                    if (isValueType(ctx.getElementType()) || isEntityType(ctx.getElementType())) {
                /*
                 * Proxies must be edited up-front so that the elements
                 * in the collection have stable identity.
                 */
                        for (Object o : value.as()) {
                            if (o == null) {
                                collection.add(null);
                            } else {
                                collection.add(editProxy(toContext, (Class<T>) ctx.getType(), (T) o));
                            }
                        }
                    } else {
                        // For simple values, just copy the values
                        collection.addAll(value.as());
                    }

                    ctx.set(collection);
                }
                return false;
            }

            @Override
            public boolean visitReferenceProperty(String propertyName, AutoBean<?> value, PropertyContext ctx) {
                value = AutoBeanUtils.getAutoBean(values.get(propertyName));
                if (value != null) {
                    if (isValueType(ctx.getType()) || isEntityType(ctx.getType())) {
                /*
                 * Value proxies must be cloned upfront, since the value
                 * is replaced outright.
                 */
                        @SuppressWarnings("unchecked")
                        AutoBean<BaseProxy> valueBean = (AutoBean<BaseProxy>) value;
                        ctx.set(editProxy(toContext, (Class<T>) ctx.getType(), (T) valueBean.as()));
                    } else {
                        ctx.set(value.as());
                    }
                }
                return false;
            }

            @Override
            public boolean visitValueProperty(String propertyName, Object value, PropertyContext ctx) {
                ctx.set(values.get(propertyName));
                return false;
            }
        });
        return clone;
    }


    /**
     * Take ownership of a proxy instance and make it editable.
     */
    private static <T extends BaseProxy> T editProxy(RequestContext ctx, Class<T> clazz, T object) {
        AutoBean<T> toClone = AutoBeanUtils.getAutoBean(object);

        // Create editable copies
        AutoBean<T> parent = toClone;

        AutoBean<T> clone = (AutoBean<T>) ctx.create(clazz);
        AutoBean<T> cloned = cloneBeanProperties(toClone, clone, ctx);

        cloned.setTag(Constants.PARENT_OBJECT, parent);
        return cloned.as();
    }


    private static boolean isEntityType(Class<?> clazz) {
        return isAssignableTo(clazz, EntityProxy.class);
    }

    private static boolean isValueType(Class<?> clazz) {
        return isAssignableTo(clazz, ValueProxy.class);
    }

    public static boolean isAssignableTo(Class<?> thisClass, Class<?> assignableTo) {
        if (thisClass == null || assignableTo == null) {
            return false;
        }

        if (thisClass.equals(assignableTo)) {
            return true;
        }

        Class<?> currentSuperClass = thisClass.getSuperclass();
        while (currentSuperClass != null) {
            if (currentSuperClass.equals(assignableTo)) {
                return true;
            }
            currentSuperClass = thisClass.getSuperclass();
        }
        return false;
    }
}
