/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.rpc.license;

public class LicenseTestUtils
{
    public static String getValidStandardLicenseString()
    {
        return RegionalLicenseHolder.VALID_STANDARD_LICENSE_KEY;
    }

    public static String getValidProfessionalLicenseString()
    {
        return RegionalLicenseHolder.VALID_PROFESSIONAL_LICENSE_KEY;
    }

    public static String getValidEnterpriseLicenseString()
    {
        return RegionalLicenseHolder.VALID_ENTERPRISE_LICENSE_KEY;
    }
}