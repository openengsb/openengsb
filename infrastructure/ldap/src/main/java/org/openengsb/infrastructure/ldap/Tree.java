package org.openengsb.infrastructure.ldap;

import java.util.Arrays;
import java.util.HashMap;

public class Tree {

    private String ou = "ou";
    private String dc = "dc";
    private String users = "users";
    private String openengsb = "openengsb";
    private String org = "org";
    private String permissionSets = "permissionSets";
    private String uid = "uid";
    private String wildcard = "%";
    private String openengsbUser = "openengsbUser";
    private String top = "top";
    private String cn = "cn";
    private String namedEntity = "namedEntity";
    private String credentials = "credentials";
    private String attributes = "attributes";
    private String permissions = "permissions";
    private String organizationalUnit = "organizationalUnit";
    private String userPassword = "userPassword";
    private String userCredentials = "userCredentials";
    private String userAttribute = "userAttribute";
    private String javaObject = "javaObject";
    private String javaClassName = "javaClassName";
    private String javaValue = "javaValue";
    private String direct = "direct";
    private String pd = "pd";
    private String userPermission = "userPermission";
    private String childrenSets = "childrenSets";
    private String psav = "psav";
    private String permissionSetAttribute = "permissionSetAttribute";

    public enum ImportantNodes { USERNODE, CREDENTIALSLEAF, USERATTRIBUTELEAF, USERPERMISSIONLEAF, USERPERMISSIONSETLEAF,
        GLOBALPERMISSIONSETNODE, GLOBALPERMISSIONLEAF, PERMISSIONSETCHILDLEAF, PERMISSIONSETATTRIBUTELEAF}

    HashMap<ImportantNodes, Node> importantNodes;

    public Tree(){
        init();
    }

    private void init(){
        
        Node root = new Node(Arrays.asList(new Attribute(dc, org)));
        Node openengsbNode = new Node(root, dc, openengsb);
        Node userParent = new Node(openengsbNode, ou, users);  
        Node globalPermissionSetsParent = new Node(openengsbNode, ou, permissionSets);
                
        Node userNode = new Node(userParent, uid, wildcard);
        userNode.setObjectClasses(Arrays.asList(openengsbUser, top));
        
        Node globalPermissionSetNode = new Node(globalPermissionSetsParent, cn, wildcard);
        globalPermissionSetNode.setObjectClasses(Arrays.asList(namedEntity, top));
        
        Node credentialsParent = new Node(userNode, ou, credentials);
        credentialsParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node attributesParent = new Node(userNode, ou, attributes);
        attributesParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node userPermissionsParent = new Node(userNode, ou, permissions);
        userPermissionsParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node userCredentialsLeaf = new Node(credentialsParent);
        userCredentialsLeaf.setAttributes(Arrays.asList(new Attribute(cn, wildcard), new Attribute(userPassword, wildcard)));
        userCredentialsLeaf.setObjectClasses(Arrays.asList(userCredentials, top));
        
        Node userAttributeLeaf = new Node(attributesParent);
        userAttributeLeaf.setAttributes(Arrays.asList(new Attribute(javaClassName, wildcard), new Attribute(javaValue, wildcard)));
        userAttributeLeaf.setObjectClasses(Arrays.asList(userAttribute, javaObject, top));
        
        Node userDirectPermissionsParent = new Node(userPermissionsParent, ou, direct);
        userDirectPermissionsParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node userPermissionSetsParent = new Node(userPermissionsParent, ou, permissionSets);
        userPermissionSetsParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node userPermissionLeaf = new Node(userDirectPermissionsParent);
        userPermissionLeaf.setAttributes(Arrays.asList(new Attribute(javaClassName, wildcard), new Attribute(pd, wildcard)));
        userPermissionLeaf.setObjectClasses(Arrays.asList(userPermission, javaObject, top));
        
        Node userPermissionSetLeaf = new Node(userPermissionSetsParent, cn, wildcard);
        userPermissionSetLeaf.setObjectClasses(Arrays.asList(namedEntity, top));
        
        Node globalDirectPermissionParent = new Node(globalPermissionSetNode, ou, direct);
        globalDirectPermissionParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node childrenPermissionSetParent = new Node(globalPermissionSetNode, ou, childrenSets);
        childrenPermissionSetParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node permissionSetAttributeParent = new Node(globalPermissionSetNode, ou, attributes);
        permissionSetAttributeParent.setObjectClasses(Arrays.asList(organizationalUnit, top));
        
        Node globalPermissionLeaf = new Node(globalDirectPermissionParent);
        globalPermissionLeaf.setAttributes(Arrays.asList(new Attribute(javaClassName, wildcard), new Attribute(javaValue, wildcard)));
        globalPermissionLeaf.setObjectClasses(Arrays.asList(userPermission, javaObject, top));
        
        Node permissionSetChildLeaf = new Node(childrenPermissionSetParent, cn, wildcard);
        permissionSetChildLeaf.setObjectClasses(Arrays.asList(namedEntity, top));
        
        Node permissionSetAttributeLeaf = new Node(permissionSetAttributeParent);
        permissionSetAttributeLeaf.setAttributes(Arrays.asList(new Attribute(cn, wildcard), new Attribute(psav, wildcard)));
        permissionSetAttributeLeaf.setObjectClasses(Arrays.asList(namedEntity, permissionSetAttribute, top));
        
        importantNodes.put(ImportantNodes.USERNODE, userNode);
        importantNodes.put(ImportantNodes.GLOBALPERMISSIONSETNODE, globalPermissionSetNode);
        importantNodes.put(ImportantNodes.CREDENTIALSLEAF, userCredentialsLeaf);
        importantNodes.put(ImportantNodes.USERATTRIBUTELEAF, userAttributeLeaf);
        importantNodes.put(ImportantNodes.USERPERMISSIONLEAF, userPermissionLeaf);
        importantNodes.put(ImportantNodes.USERPERMISSIONSETLEAF, userPermissionSetLeaf);
        importantNodes.put(ImportantNodes.GLOBALPERMISSIONLEAF, globalPermissionLeaf);
        importantNodes.put(ImportantNodes.PERMISSIONSETCHILDLEAF, permissionSetChildLeaf);
        importantNodes.put(ImportantNodes.PERMISSIONSETATTRIBUTELEAF, permissionSetAttributeLeaf);
        
    }



}
