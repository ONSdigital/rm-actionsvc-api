
SET SCHEMA 'action';


-- Function: action.createactions(integer)
-- DROP FUNCTION action.createactions(integer);

CREATE OR REPLACE FUNCTION action.createactions(p_actionplanjobPK integer)
  RETURNS boolean AS
$BODY$

DECLARE
v_text             text;
v_plan_name        text;
v_plan_description text;
v_errmess          text;
v_actionplanid     integer;
v_currentdatetime  timestamp;
v_number_of_rows   integer;

BEGIN

   SELECT j.actionplanFK FROM action.actionplanjob j WHERE j.actionplanjobPK = p_actionplanjobPK INTO v_actionplanid;

   v_currentdatetime := current_timestamp;
   --v_currentdatetime := '2016-09-09 01:00:01+01'; -- for testing


   v_number_of_rows := 0;

   -- Look at the case table to see if any cases are due to run for the actionplan passed in
   -- start date before or equal current date
   -- end date after or equal current date
   -- rules found, for plan passed in, due as days offset is less than or equal to days passed since start date (current date minus start date)
   IF EXISTS (SELECT 1
              FROM action.case c, action.actionrule r
              WHERE c.actionplanstartdate <= v_currentdatetime AND c.actionplanenddate >= v_currentdatetime
              AND r.daysoffset <= EXTRACT(DAY FROM (v_currentdatetime - c.actionplanstartdate))
              AND c.actionplanFk = v_actionplanid
              AND r.actionplanFK = c.actionplanFK) THEN

       -- Get plan description for messagelog using the actionplan passed in
      SELECT p.name, p.description
      FROM action.actionplan p
      WHERE p.actionplanPK = v_actionplanid INTO v_plan_name,v_plan_description;

      -- Collection Exercise start date reached, Run the rules due
      INSERT INTO action.action
        (
         id
        ,actionPK
        ,caseId
        ,caseFK
        ,actionplanFK
        ,actionruleFK
        ,actiontypeFK
        ,createdby
        ,manuallycreated
        ,situation
        ,stateFK
        ,createddatetime
        ,updateddatetime
        )
      SELECT
         gen_random_uuid()
        ,nextval('action.actionPKseq')
        ,l.id
        ,l.casePK
        ,l.actionplanFk
        ,l.actionrulePK
        ,l.actiontypeFK
        ,'SYSTEM'
        ,FALSE
        ,NULL
        ,'SUBMITTED'
        ,v_currentdatetime
        ,v_currentdatetime
       FROM
        (SELECT c.id
               ,c.casePK
               ,r.actionplanFK
               ,r.actionrulePK
               ,r.actiontypeFK
         FROM action.actionrule r
              ,action.case c
         WHERE  c.actionplanFk = v_actionplanid
         AND    r.actionplanFk = c.actionplanFK
         AND r.daysoffset <= EXTRACT(DAY FROM (v_currentdatetime - c.actionplanstartdate)) -- looking at start date to see if the rule is due
         AND c.actionplanstartdate <= v_currentdatetime AND c.actionplanenddate >= v_currentdatetime -- start date before or equal current date AND end date after or equal current date
         EXCEPT
         SELECT a.caseId
               ,a.caseFK
               ,a.actionplanFK
               ,a.actionruleFK
               ,a.actiontypeFK
         FROM action.action a
         WHERE a.actionplanFk = v_actionplanid) l;

      GET DIAGNOSTICS v_number_of_rows = ROW_COUNT; -- number of actions inserted

     IF v_number_of_rows > 0 THEN
         v_text := v_number_of_rows  || ' ACTIONS CREATED: ' || v_plan_description || ' (PLAN NAME: ' || v_plan_name || ') (PLAN ID: ' || v_actionplanid || ')';
         PERFORM action.logmessage(p_messagetext := v_text
                                  ,p_jobid := p_actionplanjobPK
                                  ,p_messagelevel := 'INFO'
                                  ,p_functionname := 'action.createactions');
      END IF;
   END IF;

   -- Update the date the actionplan was run on the actionplan table
   UPDATE action.actionplan
   SET lastrundatetime = v_currentdatetime
   WHERE actionplanPK  = v_actionplanid;

   -- Update the date the actionplan was run on the actionplanjob table
   UPDATE action.actionplanjob
   SET updateddatetime = v_currentdatetime
      ,stateFK = 'COMPLETED'
   WHERE actionplanjobPK =  p_actionplanjobPK
   AND   actionplanFK    =  v_actionplanid;

RETURN TRUE;

EXCEPTION

WHEN OTHERS THEN
    v_errmess := SQLSTATE;
    PERFORM action.logmessage(p_messagetext := 'CREATE ACTION(S) EXCEPTION TRIGGERED SQLERRM: ' || SQLERRM || ' SQLSTATE : ' || v_errmess
                             ,p_jobid := p_actionplanjobPK
                             ,p_messagelevel := 'FATAL'
                             ,p_functionname := 'action.createactions');
  RETURN FALSE;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


CREATE FUNCTION logmessage(p_messagetext text DEFAULT NULL::text, p_jobid numeric DEFAULT NULL::numeric, p_messagelevel text DEFAULT NULL::text, p_functionname text DEFAULT NULL::text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
v_text TEXT ;
v_function TEXT;
BEGIN
INSERT INTO action.messagelog
(messagetext, jobid, messagelevel, functionname, createddatetime )
values (p_messagetext, p_jobid, p_messagelevel, p_functionname, current_timestamp);
  RETURN TRUE;
EXCEPTION
WHEN OTHERS THEN
RETURN FALSE;
END;
$$;



CREATE SEQUENCE actionPKseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE SEQUENCE casePKseq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        MAXVALUE 999999999999
        CACHE 1;

CREATE TABLE outcomecategory
(
  handlerPK          character varying(100) NOT NULL,
  actionoutcomePK    character varying(40) NOT NULL,
  eventcategory      character varying(40)
);



CREATE TABLE action (
    id              uuid NOT NULL,
    actionPK        bigint DEFAULT nextval('actionPKseq'::regclass) NOT NULL,
    caseId          uuid NOT NULL,
    caseFK          bigint NOT NULL,
    actionplanFK    integer,
    actionruleFK    integer,
    actiontypeFK    integer NOT NULL,
    createdby       character varying(50) NOT NULL,
    manuallycreated boolean NOT NULL,
    priority        integer DEFAULT 3,
    situation       character varying(100),
    stateFK         character varying(20) NOT NULL,
    createddatetime timestamp with time zone NOT NULL,
    updateddatetime timestamp with time zone,
    optlockversion  integer DEFAULT 0
);

COMMENT ON COLUMN action.priority IS '1 = highest, 5 = lowest';



CREATE TABLE actionplan (
    id              uuid NOT NULL,
    actionplanPK    integer NOT NULL,
    name            character varying(100) NOT NULL,
    description     character varying(250) NOT NULL,
    createdby       character varying(20) NOT NULL,
    lastrundatetime timestamp with time zone
);



CREATE SEQUENCE actionplanjobseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


CREATE TABLE actionplanjob (
    id              uuid NOT NULL,
    actionplanjobPK integer DEFAULT nextval('actionplanjobseq'::regclass) NOT NULL,
    actionplanFK    integer NOT NULL,
    createdby       character varying(20) NOT NULL,
    stateFK         character varying(20) NOT NULL,
    createddatetime timestamp with time zone NOT NULL,
    updateddatetime timestamp with time zone
);



CREATE TABLE actionplanjobstate (
    statePK character varying(20) NOT NULL
);

CREATE TABLE actionrule (
    actionrulePK         integer NOT NULL,
    actionplanFK         integer NOT NULL,
    actiontypeFK         integer NOT NULL,
    name                 character varying(100) NOT NULL,
    description          character varying(250) NOT NULL,
    daysoffset           integer NOT NULL,
    priority             integer DEFAULT 3
);

COMMENT ON COLUMN actionrule.priority IS '1 = highest, 5 = lowest';



CREATE TABLE actionstate (
    statePK character varying(100) NOT NULL
);



CREATE TABLE actiontype (
    actiontypePK     integer NOT NULL,
    name             character varying(100) NOT NULL,
    description      character varying(250) NOT NULL,
    handler          character varying(100),
    cancancel        boolean NOT NULL,
    responserequired boolean
);



CREATE TABLE "case" (
    actionplanId        uuid NOT NULL,
    id	                uuid NOT NULL,
    casePK              bigint NOT NULL,
    actionplanFK        integer NOT NULL,
    actionplanstartdate timestamp with time zone NOT NULL,
    actionplanenddate   timestamp with time zone NOT NULL
);



CREATE SEQUENCE messageseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;



CREATE TABLE messagelog (
    messagePK       bigint DEFAULT nextval('messageseq'::regclass) NOT NULL,
    messagetext     character varying,
    jobid           numeric,
    messagelevel    character varying,
    functionname    character varying,
    createddatetime timestamp with time zone
);


-- Add primary Keys

ALTER TABLE ONLY action             ADD CONSTRAINT actionPK_pkey                PRIMARY KEY (actionPK);
ALTER TABLE ONLY actionplan         ADD CONSTRAINT actionplanPK_pkey            PRIMARY KEY (actionplanPK);
ALTER TABLE ONLY actionplanjob      ADD CONSTRAINT actionplanjobPK_pkey         PRIMARY KEY (actionplanjobPK);
ALTER TABLE ONLY actionplanjobstate ADD CONSTRAINT statePK_pkey                 PRIMARY KEY (statePK);
ALTER TABLE ONLY actionrule         ADD CONSTRAINT actionrulePK_pkey            PRIMARY KEY (actionrulePK);
ALTER TABLE ONLY actionstate        ADD CONSTRAINT tatePK_pkey                  PRIMARY KEY (statePK);
ALTER TABLE ONLY actiontype         ADD CONSTRAINT actiontypePK_pkey            PRIMARY KEY (actiontypePK);
ALTER TABLE ONLY messagelog         ADD CONSTRAINT messagePK_pkey               PRIMARY KEY (messagePK);
ALTER TABLE ONLY outcomecategory    ADD CONSTRAINT outcomecategory_pkey         PRIMARY KEY (handlerPK, actionoutcomePK);

ALTER TABLE ONLY "case"             ADD CONSTRAINT casePK_pkey                  PRIMARY KEY (casePK);



-- Add Foreign Keys

ALTER TABLE ONLY actionplanjob      ADD CONSTRAINT actionplanFK_fkey       FOREIGN KEY (actionplanFK) REFERENCES actionplan (actionplanPK);
ALTER TABLE ONLY actionplanjob      ADD CONSTRAINT actionplanjobstate_fkey FOREIGN KEY (stateFK)      REFERENCES actionplanjobstate (statePK);

ALTER TABLE ONLY action             ADD CONSTRAINT actiontypeFK_fkey       FOREIGN KEY (actiontypeFK) REFERENCES actiontype (actiontypePK);
ALTER TABLE ONLY action             ADD CONSTRAINT actionplanFK_fkey       FOREIGN KEY (actionplanFK) REFERENCES actionplan (actionplanPK);
ALTER TABLE ONLY action             ADD CONSTRAINT actionruleFK_fkey       FOREIGN KEY (actionruleFK) REFERENCES actionrule (actionrulePK);

ALTER TABLE ONLY action             ADD CONSTRAINT actionstate_fkey        FOREIGN KEY (stateFK)      REFERENCES actionstate(statePK);

ALTER TABLE ONLY actionrule         ADD CONSTRAINT actiontypeFK_fkey       FOREIGN KEY (actiontypeFK) REFERENCES actiontype (actiontypePK);
ALTER TABLE ONLY actionrule         ADD CONSTRAINT actionplanFK_fkey       FOREIGN KEY (actionplanFK) REFERENCES actionplan (actionplanPK);

ALTER TABLE ONLY "case"             ADD CONSTRAINT actionplanFK_fkey       FOREIGN KEY (actionplanFK) REFERENCES action.actionplan (actionplanPK);


-- Add index
ALTER TABLE action        ADD CONSTRAINT actionid_uuid_key        UNIQUE (id);
ALTER TABLE actionplan    ADD CONSTRAINT actionplanid_uuid_key    UNIQUE (id);
ALTER TABLE "case"        ADD CONSTRAINT caseid_uuid_key          UNIQUE (id);
ALTER TABLE actionplanjob ADD CONSTRAINT actionplanjobid_uuid_key UNIQUE (id);

