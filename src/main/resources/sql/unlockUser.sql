update ad_user
set islocked='N',
    dateaccountlocked= null,
    failedlogincount=0
where ad_user_id = ?