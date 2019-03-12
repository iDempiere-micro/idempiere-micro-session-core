update ad_user
set islocked='Y',
    dateaccountlocked=current_timestamp
where ad_user_id = ?