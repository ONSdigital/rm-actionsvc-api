
-- Sequence: action.reportPKseq
-- DROP SEQUENCE action.reportPKseq;

CREATE SEQUENCE action.reportPKseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;


-- Table: action.reporttype
-- DROP TABLE action.reporttype;

CREATE TABLE action.reporttype
(
    reporttypePK  character varying (20),
    displayorder  integer,
    displayname   character varying(40),
    CONSTRAINT reporttype_pkey PRIMARY KEY (reporttypePK)
);



-- Table: action.report
-- DROP TABLE action.report;

CREATE TABLE action.report
(
    id             uuid NOT NULL,
    reportPK       bigint NOT NULL,
    reporttypeFK   character varying (20),
    contents       text ,
    createddatetime timestamp with time zone,
    CONSTRAINT report_pkey PRIMARY KEY (reportpk),
    CONSTRAINT report_uuid_key UNIQUE (id),
    CONSTRAINT reporttypefk_fkey FOREIGN KEY (reporttypefk)
    REFERENCES action.reporttype (reporttypepk) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Function: action.logmessage(text, numeric, text, text)

-- DROP FUNCTION action.logmessage(text, numeric, text, text);

CREATE OR REPLACE FUNCTION action.logmessage(p_messagetext text DEFAULT NULL::text, p_jobid numeric DEFAULT NULL::numeric, p_messagelevel text DEFAULT NULL::text, p_functionname text DEFAULT NULL::text)
  RETURNS boolean AS
$BODY$
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
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




-- Insert reports into report tables 
INSERT INTO action.reporttype(reporttypePK,displayorder,displayname) VALUES('ACTIONS',10,'Action Status');
INSERT INTO action.reporttype(reporttypePK,displayorder,displayname) VALUES('ACTIONPLANS',20,'Action Plans');

-- Function: action.generate_action_mi()

-- DROP FUNCTION action.generate_action_mi();

CREATE OR REPLACE FUNCTION action.generate_action_mi()
  RETURNS boolean AS
$BODY$
DECLARE

v_contents      text;
r_dataline      record;
v_rows          integer;

BEGIN
    
    PERFORM action.logmessage(p_messagetext := 'GENERATING ACTION MI REPORTS'
                              ,p_jobid := 0
                              ,p_messagelevel := 'INFO'
                              ,p_functionname := 'action.generate_action_mi');  
    
       v_rows     := 0;
       v_contents := '';
       v_contents := 'actionplan,action_plan_name,action_type,action_plan_startdate,daysoffset,handler,cnt,action_state';

-- Action State Report

       FOR r_dataline IN (SELECT  template.actionplan                       AS actionplan
                                , template.plan_description                 AS action_plan_name
                                , template.type_description                 AS action_type
                                , action_case_cnt.actionplanstartdate::date AS action_plan_startdate      
                                , template.daysoffset                       AS daysoffset
                                , template.handler                          AS handler
                                , COALESCE(action_case_cnt.cnt,0) AS cnt
                                , action_case_cnt.actionstate     AS action_state     
                          FROM (SELECT COALESCE(cases.actionplanFK,actions.actionplanFK) AS  actionplan
                                     , cases.actionplanstartdate
                                     , actions.createddatetime
                                     , COALESCE(cases.actionrulePK,actions.actionruleFK) AS actionrule
                                     , COALESCE(cases.actiontypeFK,actions.actiontypeFK) AS actiontype
                                     , actions.stateFK                                   AS actionstate
                                     , COUNT(*) cnt
                                FROM (SELECT  c.actionplanFK
                                            , c.actionplanstartdate::DATE
                                            , c.casePK
                                            , r.actionrulePK
                                            , r.actiontypeFK
                                      FROM  action.case c
                                          , action.actionrule r
                                      WHERE c.actionplanFK = r.actionplanFK) cases
                                      FULL JOIN (SELECT a.actionplanFK
                                                      , a.createddatetime::DATE
                                                      , a.caseFK
                                                      , a.actionruleFK
                                                      , a.actiontypeFK
                                                      , a.stateFK
                                                 FROM action.action a) actions
                                      ON (actions.actionplanFK = cases.actionplanFK 
                                      AND actions.actionruleFK = cases.actionrulePK
                                      AND actions.actiontypeFK = cases.actiontypeFK
                                      AND actions.caseFK       = cases.casePK) 
                              GROUP BY actionplan, actionplanstartdate, actionrule, actiontype, actionstate, createddatetime) action_case_cnt 
                              FULL JOIN (SELECT  r.actionplanFK AS actionplan
                                               , r.actionrulePK AS actionrule
                                               , r.actiontypeFK AS actiontype
                                               , p.description  AS plan_description
                                               , t.description  AS type_description
                                               , r.daysoffset  
                                               , t.handler    
                                         FROM   action.actionplan p
                                              , action.actionrule r
                                              , action.actiontype t
                                         WHERE p.actionplanPK = r.actionplanFK 
                                         AND   r.actiontypeFK = t.actiontypePK) template
                                         ON (template.actionplan = action_case_cnt.actionplan 
                                         AND template.actiontype = action_case_cnt.actiontype
                                         AND template.actionrule = action_case_cnt.actionrule)
                                         ORDER BY template.actionplan,template.daysoffset,template.plan_description,action_plan_startdate) LOOP

                           v_contents := v_contents                         || chr(10) 
                           || r_dataline.actionplan                         || ','
                           || r_dataline.action_plan_name                   || ','
                           || r_dataline.action_type                        || ','
                           || COALESCE(r_dataline.action_plan_startdate::text,'') || ','
                           || r_dataline.daysoffset                         || ','
                           || r_dataline.handler                            || ','
                           || r_dataline.cnt                                || ','
                           || COALESCE(r_dataline.action_state,'') ;   
             v_rows := v_rows+1;  
       END LOOP;       

       -- Insert the data into the report table
       INSERT INTO action.report (id, reportPK,reporttypeFK,contents, createddatetime) VALUES(gen_random_uuid(), nextval('action.reportPKseq'), 'ACTIONS', v_contents, CURRENT_TIMESTAMP); 

               
       PERFORM action.logmessage(p_messagetext := 'GENERATING ACTIONS MI REPORT COMPLETED ROWS WRIITEN = ' || v_rows
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'action.generate_action_mi'); 
      
    
       PERFORM action.logmessage(p_messagetext := 'ACTIONS MI REPORT GENERATED'
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'action.generate_action_mi'); 

-- Action Plan Report

       -- Reset variables
       v_rows     := 0;
       v_contents := '';
       v_contents := 'actionplan,action_plan_name,action_type,action_plan_start_date,daysoffset,handler';

       FOR r_dataline IN (SELECT DISTINCT p.actionplanPK                AS actionplan
                                        , p.description  		AS action_plan_name
                                        , t.description			AS action_type
                                        , c.actionplanstartdate::date 	AS action_plan_startdate
                                        , r.daysoffset  		AS daysoffset
                                        , t.handler  			AS handler
                          FROM   action.case c
                          RIGHT OUTER JOIN action.actionrule r  ON c.actionplanFK = r.actionplanFK
                          INNER JOIN action.actionplan p ON r.actionplanFK = p.actionplanPK
                          INNER JOIN action.actiontype t ON r.actiontypeFK = t.actiontypePK
                          ORDER BY p.actionplanPK, r.daysoffset, p.description,4 ) LOOP
                          
                          v_contents := v_contents                         || chr(10) 
                          || r_dataline.actionplan                         || ','
                          || r_dataline.action_plan_name                   || ','
                          || r_dataline.action_type                        || ','
                          || COALESCE(r_dataline.action_plan_startdate::text,'') || ','
                          || r_dataline.daysoffset                         || ','
                          || r_dataline.handler;                                   
             v_rows := v_rows+1;  
       END LOOP;       

       -- Insert the data into the report table
       INSERT INTO action.report (id, reportPK,reporttypeFK,contents, createddatetime) VALUES(gen_random_uuid(), nextval('action.reportPKseq'), 'ACTIONPLANS', v_contents, CURRENT_TIMESTAMP); 

        
       PERFORM action.logmessage(p_messagetext := 'GENERATING ACTION PLAN MI REPORT COMPLETED ROWS WRIITEN = ' || v_rows
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'action.generate_action_mi'); 
      
    
       PERFORM action.logmessage(p_messagetext := 'ACTION PLAN MI REPORT GENERATED'
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'action.generate_action_mi'); 
  RETURN TRUE;

  EXCEPTION
  WHEN OTHERS THEN   
     PERFORM action.logmessage(p_messagetext := 'GENERATE REPORTS EXCEPTION TRIGGERED SQLERRM: ' || SQLERRM || ' SQLSTATE : ' || SQLSTATE
                               ,p_jobid := 0
                               ,p_messagelevel := 'FATAL'
                               ,p_functionname := 'action.generate_action_mi');
                               
  RETURN FALSE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  COST 100;