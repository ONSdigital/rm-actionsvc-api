SELECT DISTINCT  p.actionplanPK               AS actionplan
               , p.description  	      AS action_plan_name
               , t.description		      AS action_type
               , c.actionplanstartdate::date  AS action_plan_startdate
               , r.daysoffset  		      AS daysoffset
               , t.handler  		     AS handler
FROM   action.case c
RIGHT OUTER JOIN action.actionrule r  ON c.actionplanFK = r.actionplanFK
INNER JOIN action.actionplan p ON r.actionplanFK = p.actionplanPK
INNER JOIN action.actiontype t ON r.actiontypeFK = t.actiontypePK
ORDER BY p.actionplanPK, r.daysoffset, p.description,4