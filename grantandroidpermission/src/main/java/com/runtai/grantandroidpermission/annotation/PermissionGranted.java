package com.runtai.grantandroidpermission.annotation;

import com.runtai.grantandroidpermission.CheckAnnotatePermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermissionGranted {
  int requestCode() default CheckAnnotatePermission.DEFAULT_REQUEST_CODE;
}
