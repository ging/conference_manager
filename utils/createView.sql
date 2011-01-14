USE cmevents;
CREATE OR REPLACE VIEW `ConferenceTime` AS 
select 
    `ss`.`CONFERENCE_ID` AS `CONFERENCE_ID`,
    (min(`ss`.`START_TIME`) + interval -(100) second) AS `START_TIME`,
    max(`ss`.`STOP_TIME`) AS `STOP_TIME`,
    (case `c`.`ENABLE_SIP` when 1 then 3 else 2 end) AS `ISABEL_RESOURCES`,
    1 AS `VNC_RESOURCES`,
    0 AS `SPY_RESOURCES` 
from (`Session` `ss` join `Conference` `c`) 
where (`c`.`CONFERENCE_ID` = `ss`.`CONFERENCE_ID`) group by `ss`.`CONFERENCE_ID`;