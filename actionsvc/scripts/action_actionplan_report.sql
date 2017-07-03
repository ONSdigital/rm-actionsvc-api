SELECT DISTINCT
         p.actionplanPK 
       , p.name 			AS action_plan_name
       , t.description			AS action_type
       , c.actionplanstartdate::DATE 	AS action_plan_start_date
       , r.daysoffset  			AS daysoffset
       , t.handler  			AS handler
 FROM   action.case c
 RIGHT OUTER JOIN action.actionrule r  ON c.actionplanFK = r.actionplanFK
 INNER JOIN action.actionplan p ON r.actionplanFK = p.actionplanPK
 INNER JOIN action.actiontype t ON r.actiontypeFK = t.actiontypePK
 ORDER BY p.actionplanPK 
 
