update ad_user
set islocked=?,
    failedlogincount=?,
    dateaccountlocked=?
where ad_user_id = ?