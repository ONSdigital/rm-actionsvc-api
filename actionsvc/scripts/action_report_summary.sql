SELECT  template.actionplan
      , template.plan_description action_plan_name
      , template.type_description action_type
     -- , CASE WHEN action_case_cnt.actionplanstartdate IS NULL THEN 'NO' ELSE 'YES' END AS cases_created
      , action_case_cnt.actionplanstartdate AS action_plan_startdate      
      , template.daysoffset
      , template.handler
      , COALESCE(action_case_cnt.cnt,0) AS cnt
      , action_case_cnt.actionstate     AS action_state      
  --  , (action_case_cnt.actionplanstartdate + (template.daysoffset || ' days')::interval)::date AS rule_date  
  --  , now()::DATE AS report_date
  --  , EXTRACT(DAY FROM (now()- action_case_cnt.actionplanstartdate)) daysoffset_ruledate
  --  , CASE WHEN EXTRACT(DAY FROM (now()- action_case_cnt.actionplanstartdate)) >= template.daysoffset THEN 'YES' 
  --    ELSE
  --       CASE WHEN  action_case_cnt.actionplanstartdate IS NOT NULL THEN 'NO' END  
  --    END AS rule_due
  --  , CASE WHEN action_case_cnt.actionstate IS NULL THEN 'NO' ELSE 'YES' END         AS action_created
  --  , action_case_cnt.createddatetime AS action_createddate
  
        
  -------------------------------------------------------------------
      -- for testing  ,'2017-11-23 01:00:01+01'::DATE AS report_date
  -------------------------------------------------------------------
  -------------------------------------------------------------------
      -- for testing ,EXTRACT(DAY FROM ('2017-11-23 01:00:01+01'::TIMESTAMP WITH TIME ZONE- action_case_cnt.actionplanstartdate)) daysoffset_ruledate
  -------------------------------------------------------------------
  -------------------------------------------------------------------
      -- for testing 
    --, CASE WHEN EXTRACT(DAY FROM ('2017-11-23 01:00:01+01'::TIMESTAMP WITH TIME ZONE - action_case_cnt.actionplanstartdate)) >= template.daysoffset THEN 'YES' 
    --    ELSE
    --       CASE WHEN  action_case_cnt.actionplanstartdate IS NOT NULL THEN 'NO' END  
    --    END AS rule_due
  -------------------------------------------------------------------             
     
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
      ORDER BY template.actionplan,template.daysoffset,template.plan_description,action_plan_startdate