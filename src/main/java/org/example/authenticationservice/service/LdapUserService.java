package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.entity.EmployeeRole;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LdapUserService {

    private final LdapTemplate ldapTemplate;

    public String createUser(UUID organizationId, String username, String password, String firstName, String lastName, String email, EmployeeRole role) {
        createOrganizationUnit(organizationId);
        Name userDn = LdapNameBuilder.newInstance().add("ou", "organizations").add("ou", organizationId.toString()).add("uid", username).build();
        Attributes attributes = new BasicAttributes();
        Attribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("top");
        objectClass.add("person");
        objectClass.add("organizationalPerson");
        objectClass.add("inetOrgPerson");
        attributes.put(objectClass);
        attributes.put("uid", username);
        attributes.put("cn", firstName + " " + lastName);
        attributes.put("sn", lastName);
        attributes.put("givenName", firstName);
        attributes.put("mail", email);
        attributes.put("userPassword", password);
        attributes.put("employeeType", role.name());
        ldapTemplate.bind(userDn, null, attributes);
        return username;
    }

    private void createOrganizationUnit(UUID organizationId) {
        Name organizationDn = LdapNameBuilder.newInstance().add("ou", "organizations").add("ou", organizationId.toString()).build();
        Attributes attributes = new BasicAttributes();
        Attribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("top");
        objectClass.add("organizationalUnit");
        attributes.put(objectClass);
        attributes.put("ou", organizationId.toString());
        try {
            ldapTemplate.bind(organizationDn, null, attributes);
        } catch (NameAlreadyBoundException e) {
            // organizational unit already exists, nothing to do
        }
    }

    public boolean authenticate(String username, String password, UUID organizationId) {
        String base = "ou=" + organizationId + ",ou=organizations";

        LdapQuery query = LdapQueryBuilder.query()
                .base(base)
                .where("objectClass").is("inetOrgPerson")
                .and("uid").is(username);

        try {
            ldapTemplate.authenticate(query, password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}