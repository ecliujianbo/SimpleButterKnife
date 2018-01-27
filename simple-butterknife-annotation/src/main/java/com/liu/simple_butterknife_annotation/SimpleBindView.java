package com.liu.simple_butterknife_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只处理绑定控件，只存在源码期
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SimpleBindView {
    int value();
}
