SELECT u.Name || '@' || c.Name || '.' || o.Name AS Text, u.c_bpartner_id
FROM AD_User u,
     AD_Client c,
     AD_Org o,
     AD_User_Roles ur
WHERE u.AD_User_ID=?
  AND c.AD_Client_ID = u.ad_client_id
  AND ur.AD_User_ID = u.AD_User_ID
  AND (o.AD_Client_ID = 0 OR o.AD_Client_ID = c.AD_Client_ID)
  AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID = ur.AD_Role_ID)
  AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID = ur.AD_Role_ID)