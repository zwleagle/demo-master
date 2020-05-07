package com.zwl.demo.security.compoment;

import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;

import java.io.IOException;
import java.nio.file.DirectoryStream;

public class DynamicSecurityFilter extends AbstractSecurityInterceptor implements DirectoryStream.Filter {
    @Override
    public boolean accept(Object entry) throws IOException {
        return false;
    }

    @Override
    public Class<?> getSecureObjectClass() {
        return null;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return null;
    }
}
