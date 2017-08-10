-- ACTION SERVICE

-- correct primary key name
ALTER TABLE action.actionstate DROP CONSTRAINT tatepk_pkey CASCADE;
ALTER TABLE action.actionstate ADD  CONSTRAINT actionstatepk_pkey PRIMARY KEY(statepk);


-- Add back the Foreign Key
ALTER TABLE ONLY action.action ADD CONSTRAINT actionstatefk_fkey FOREIGN KEY (stateFK) REFERENCES action.actionstate(statePK);


-----------------------------------------------------------------------------
-----------------------------------------------------------------------------


-- actionplanjob

-- Index: action.actionplanjob_actionplanfk_index
-- DROP INDEX action.actionplanjob_actionplanfk_index;

CREATE INDEX actionplanjob_actionplanfk_index ON action.actionplanjob USING btree (actionplanfk);


-- Index: action.actionplanjob_statefk_index
-- DROP INDEX action.actionplanjob_statefk_index;

CREATE INDEX actionplanjob_statefk_index ON action.actionplanjob USING btree (statefk);


-----------------------------------------------------------------------------
-----------------------------------------------------------------------------

-- action table


-- Index: action.action_actionplanfk_index
-- DROP INDEX action.action_actionplanfk_index;

CREATE INDEX action_actionplanfk_index ON action.action USING btree (actionplanfk);


-- Index: action.action_actionrulefk_index
-- DROP INDEX action.action_actionrulefk_index;

CREATE INDEX action_actionrulefk_index ON action.action USING btree (actionrulefk);


-- Index: action.action_actiontypefk_index
-- DROP INDEX action.action_actiontypefk_index;

CREATE INDEX action_actiontypefk_index ON action.action USING btree (actiontypefk);


-- Index: action.action_statefk_index
-- DROP INDEX action_statefk_index;

CREATE INDEX action_statefk_index ON action.action USING btree (statefk);


-- Index: action.action_optlockversion_index
-- DROP INDEX action.action_optlockversion_index;

CREATE INDEX action_optlockversion_index ON action.action USING btree (optlockversion);


-----------------------------------------------------------------------------
-----------------------------------------------------------------------------

-- actionrule table 

-- Index: action.actionrule_actionplanfk_index
-- DROP INDEX action.actionrule_actionplanfk_index;

CREATE INDEX actionrule_actionplanfk_index ON action.actionrule USING btree (actionplanfk);


-- Index: action.actionrule_actiontypefk_index
-- DROP INDEX action.actionrule_actiontypefk_index;

CREATE INDEX actionrule_actiontypefk_index ON action.actionrule USING btree (actiontypefk);

-----------------------------------------------------------------------------
-----------------------------------------------------------------------------

-- actiontype table

-- Index: action.actiontype_name_index
-- DROP INDEX action.actiontype_name_index;

CREATE INDEX actiontype_name_index ON action.actiontype USING btree (name);


-----------------------------------------------------------------------------
-----------------------------------------------------------------------------


-- case table

-- Index: action.actioncase_actionplanfk_index
-- DROP INDEX action.actioncase_actionplanfk_index;

CREATE INDEX case_actionplanfk_index ON action."case" USING btree (actionplanfk);