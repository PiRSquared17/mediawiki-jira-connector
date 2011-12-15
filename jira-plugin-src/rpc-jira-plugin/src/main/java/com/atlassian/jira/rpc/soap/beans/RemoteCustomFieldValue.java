package com.atlassian.jira.rpc.soap.beans;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class RemoteCustomFieldValue
{
    private String customfieldId;
    private String key;
    private String[] values;

    public RemoteCustomFieldValue()
    {
    }

    /**
     * Construct a custom field value proxy object.
     *
     * @param customfieldId The string ID of an existing custom field,
     *                      of the form 'customfield_[id]' where [id] is the database ID of
     *                      the custom field.  This ID can be determined from the database,
     *                      or by viewing a custom field in the admin interface, and copying
     *                      its ID from the URL.
     */
    public RemoteCustomFieldValue(String customfieldId, String value)
    {
        this(customfieldId, null, new String[] { value });
    }

    /**
     * Construct a custom field value proxy object.
     *
     * @param customfieldId The string ID of an existing custom field,
     *                      of the form 'customfield_[id]' where [id] is the database ID of
     *                      the custom field.  This ID can be determined from the database,
     *                      or by viewing a custom field in the admin interface, and copying
     *                      its ID from the URL.
     */
    public RemoteCustomFieldValue(String customfieldId, String[] values)
    {
        this(customfieldId, null, values);
    }

    /**
     * Construct a custom field value proxy object.
     *
     * @param customfieldId The string ID of an existing custom field,
     *                      of the form 'customfield_[id]' where [id] is the database ID of
     *                      the custom field.  This ID can be determined from the database,
     *                      or by viewing a custom field in the admin interface, and copying
     *                      its ID from the URL.
     * @param parentKey     Used for multi-dimensional custom fields such as Cascading select lists. Null in other cases
     * @param values        Array of strings representing the value of the custom field. For single text field values, there will
     *                      only be a single item in the array (e.g. TextFieldCFType) and multiple values for list fields (e.g. MultipleSelectCFType)
     */
    public RemoteCustomFieldValue(String customfieldId, String parentKey, String[] values)
    {
        this.customfieldId = customfieldId;
        this.key = parentKey;
        this.values = values;
    }


    public String getCustomfieldId()
    {
        return customfieldId;
    }

    /**
     * Sets the custom field id.
     *
     * @param customfieldId The string ID of an existing custom field,
     *                      of the form 'customfield_[id]' where [id] is the database ID of
     *                      the custom field.  This ID can be determined from the database,
     *                      or by viewing a custom field in the admin interface, and copying
     *                      its ID from the URL.
     */
    public void setCustomfieldId(String customfieldId)
    {
        this.customfieldId = customfieldId;
    }


    public String[] getValues()
    {
        return values;
    }

    public void setValues(String[] values)
    {
        this.values = values;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RemoteCustomFieldValue))
        {
            return false;
        }
        RemoteCustomFieldValue rhs = (RemoteCustomFieldValue) o;
        return new EqualsBuilder()
                .append(getCustomfieldId(), rhs.getCustomfieldId())
                .append(getKey(), rhs.getKey())
                .append(getValues(), rhs.getValues())
                .isEquals();
    }

    public int compareTo(Object obj)
    {
        RemoteCustomFieldValue o = (RemoteCustomFieldValue) obj;
        return new CompareToBuilder()
                .append(getCustomfieldId(), o.getCustomfieldId())
                .toComparison();
    }

    public int hashCode()
    {
        return new HashCodeBuilder(755, 13)
                .append(getCustomfieldId())
                .append(getKey())
                .append(getValues())
                .toHashCode();
    }
}
