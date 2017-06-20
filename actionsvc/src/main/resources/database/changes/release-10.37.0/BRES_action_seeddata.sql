SET SCHEMA 'action';

-- outcomecategory
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Field'  ,'REQUEST_COMPLETED'            ,'ACTION_COMPLETED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Field'  ,'REQUEST_COMPLETED_DEACTIVATE' ,'ACTION_COMPLETED_DEACTIVATED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Field'  ,'REQUEST_COMPLETED_DISABLE'    ,'ACTION_COMPLETED_DISABLED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Notify' ,'REQUEST_COMPLETED'            ,'ACTION_COMPLETED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Notify' ,'REQUEST_COMPLETED_DEACTIVATE' ,'ACTION_COMPLETED_DEACTIVATED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Notify' ,'REQUEST_COMPLETED_DISABLE'    ,'ACTION_COMPLETED_DISABLED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Printer','REQUEST_COMPLETED'            ,'ACTION_COMPLETED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Printer','REQUEST_COMPLETED_DEACTIVATE' ,'ACTION_COMPLETED_DEACTIVATED');
INSERT INTO outcomecategory (handlerPK, actionoutcomePK, eventcategory) VALUES ('Printer','REQUEST_COMPLETED_DISABLE'    ,'ACTION_COMPLETED_DISABLED');


-- Action Types
INSERT INTO action.actiontype (actiontypePK, name, description, handler, cancancel, responserequired) VALUES (1,'BRESEL' ,'Enrolment Invitation Letter','Printer','n','n');
INSERT INTO action.actiontype (actiontypePK, name, description, handler, cancancel, responserequired) VALUES (2,'BRESERL','Enrolment Reminder Letter','Printer','n','n');
INSERT INTO action.actiontype (actiontypePK, name, description, handler, cancancel, responserequired) VALUES (3,'BRESSNE','Survey Reminder Notification','Notify','n','n');

-- Action Plans
INSERT INTO action.actionplan (id, actionplanPK, name, description, createdby, lastrundatetime) VALUES ('e71002ac-3575-47eb-b87f-cd9db92bf9a7',1,'Enrolment','BRES Enrolment','SYSTEM',NULL);
INSERT INTO action.actionplan (id, actionplanPK, name, description, createdby, lastrundatetime) VALUES ('0009e978-0932-463b-a2a1-b45cb3ffcb2a',2,'BRES','BRES','SYSTEM',NULL);

-- Action Rules
INSERT INTO action.actionrule (actionrulePK, actionplanFK, actiontypeFK, name, description, daysoffset) VALUES (1,1,1,'NULL','NULL',1);
INSERT INTO action.actionrule (actionrulePK, actionplanFK, actiontypeFK, name, description, daysoffset) VALUES (2,1,2,'NULL','NULL',85);
INSERT INTO action.actionrule (actionrulePK, actionplanFK, actiontypeFK, name, description, daysoffset) VALUES (3,1,2,'NULL','NULL',127);

INSERT INTO action.actionrule (actionrulePK, actionplanFK, actiontypeFK, name, description, daysoffset) VALUES (4,2,3,'NULL','NULL',85);
INSERT INTO action.actionrule (actionrulePK, actionplanFK, actiontypeFK, name, description, daysoffset) VALUES (5,2,3,'NULL','NULL',127);


-- Create actionrule description and name from the action type
UPDATE action.actionrule ar
SET description =  (SELECT t1.description || '(+' || ar.daysoffset || ' days)' FROM action.actiontype t1 WHERE ar.actiontypeFK = t1.actiontypePK) 
   ,name        =  (SELECT t2.name || '+' || ar.daysoffset FROM action.actiontype t2 WHERE ar.actiontypeFK = t2.actiontypePK)
WHERE EXISTS (SELECT 1 FROM action.actiontype t3 WHERE ar.actiontypeFK = t3.actiontypePK);



-- actionstate
INSERT INTO actionstate (statePK) VALUES ('SUBMITTED');
INSERT INTO actionstate (statePK) VALUES ('PENDING');
INSERT INTO actionstate (statePK) VALUES ('ACTIVE');
INSERT INTO actionstate (statePK) VALUES ('COMPLETED');
INSERT INTO actionstate (statePK) VALUES ('CANCEL_SUBMITTED');
INSERT INTO actionstate (statePK) VALUES ('CANCELLED');
INSERT INTO actionstate (statePK) VALUES ('CANCEL_PENDING');
INSERT INTO actionstate (statePK) VALUES ('CANCELLING');
INSERT INTO actionstate (statePK) VALUES ('ABORTED');

-- actionplanjobstate
INSERT INTO actionplanjobstate (statePK) VALUES ('SUBMITTED');
INSERT INTO actionplanjobstate (statePK) VALUES ('STARTED');
INSERT INTO actionplanjobstate (statePK) VALUES ('COMPLETED');
INSERT INTO actionplanjobstate (statePK) VALUES ('FAILED');






