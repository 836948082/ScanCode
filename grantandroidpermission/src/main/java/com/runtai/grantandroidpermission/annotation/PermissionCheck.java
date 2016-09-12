package com.runtai.grantandroidpermission.annotation;

import com.runtai.grantandroidpermission.CheckAnnotatePermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionCheck {
    int requestCode() default CheckAnnotatePermission.DEFAULT_REQUEST_CODE;
}
