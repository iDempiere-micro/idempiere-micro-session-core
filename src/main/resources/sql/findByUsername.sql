SELECT ad_user_id,             -- 1
       ad_client_id,           -- 2
       ad_org_id,              -- 3
       isactive,               -- 4
       created,                -- 5
       createdby,              -- 6
       updated,                -- 7
       updatedby,              -- 8
       name,                   -- 9
       description,            -- 10
       password,               -- 11
       email,                  -- 12
       supervisor_id,          -- 13
       c_bpartner_id,          -- 14
       processing,             -- 15
       emailuser,              -- 16
       emailuserpw,            -- 17
       c_bpartner_location_id, -- 18
       c_greeting_id,          -- 19
       title,                  -- 20
       comments,               -- 21
       phone,                  -- 22
       phone2,                 -- 23
       fax,                    -- 24
       lastcontact,            -- 25
       lastresult,             -- 26
       birthday,               -- 27
       ad_orgtrx_id,           -- 28
       emailverify,            -- 29
       emailverifydate,        -- 30
       notificationtype,       -- 31
       isfullbpaccess,         -- 32
       c_job_id,               -- 33
       ldapuser,               -- 34
       connectionprofile,      -- 35
       value,                  -- 36
       userpin,                -- 37
       isinpayroll,            -- 38
       ad_user_uu,             -- 39
       ismenuautoexpand,       -- 40
       salt,                   -- 41
       islocked,               -- 42
       dateaccountlocked,      -- 43
       failedlogincount,       -- 44
       datepasswordchanged,    -- 45
       datelastlogin,          -- 46
       isnopasswordreset,      -- 47
       isexpired,              -- 48
       securityquestion,       -- 49
       answer,                 -- 50
       issaleslead,
       c_location_id,
       leadsource,
       leadstatus,
       leadsourcedescription,
       leadstatusdescription,
       c_campaign_id,
       salesrep_id,
       bpname,
       bp_location_id,
       isaddmailtextautomatically,
       r_defaultmailtext_id,
       ad_image_id

FROM ad_user
where Password IS NOT NULL AND
      COALESCE(LDAPUser, Name) =?
   or EMail=?
  and
      EXISTS(SELECT *
             FROM AD_User_Roles ur
                    INNER JOIN AD_Role r ON (ur.AD_Role_ID = r.AD_Role_ID)
             WHERE ur.AD_User_ID = AD_User.AD_User_ID
               AND ur.IsActive = 'Y'
               AND r.IsActive = 'Y') AND
      EXISTS(SELECT *
             FROM AD_Client c
             WHERE c.AD_Client_ID = AD_User.AD_Client_ID
               AND c.IsActive = 'Y') AND
      AD_User.IsActive = 'Y'
order by ad_user_id