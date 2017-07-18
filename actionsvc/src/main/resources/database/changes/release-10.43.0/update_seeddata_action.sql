set schema 'action';

UPDATE action.actiontype
SET name = 'BSNOT'
WHERE name = 'BRESEL';


UPDATE action.actiontype
SET name = 'BSREM'
WHERE name = 'BRESERL';


UPDATE action.actiontype
SET name = 'BSSNE'
WHERE name = 'BRESSNE';


-- Create actionrule description and name from the action type
UPDATE action.actionrule ar
SET description =  (SELECT t1.description || '(+' || ar.daysoffset || ' days)' FROM action.actiontype t1 WHERE ar.actiontypeFK = t1.actiontypePK) 
   ,name        =  (SELECT t2.name || '+' || ar.daysoffset FROM action.actiontype t2 WHERE ar.actiontypeFK = t2.actiontypePK)
WHERE EXISTS (SELECT 1 FROM action.actiontype t3 WHERE ar.actiontypeFK = t3.actiontypePK);

