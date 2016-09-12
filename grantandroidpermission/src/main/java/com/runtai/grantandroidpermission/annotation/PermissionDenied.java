package com.runtai.grantandroidpermission.annotation;

import com.runtai.grantandroidpermission.CheckAnnotatePermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionDenied {
  int requestCode() default CheckAnnotatePermission.DEFAULT_REQUEST_CODE;
}
