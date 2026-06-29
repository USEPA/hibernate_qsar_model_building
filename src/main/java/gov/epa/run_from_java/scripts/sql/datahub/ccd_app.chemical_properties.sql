select * from ccd_app.chemical_properties cp
where source like 'ACD%' and dtxsid='DTXSID001108150';


select distinct name,unit,source from ccd_app.chemical_properties cp
where name not in ('Boiling Point','Density','Flash Point','Melting Point','Vapor Pressure','Water Solubility',
                  'Henry''s Law','Surface Tension')
order by name,unit ;


select distinct name from ccd_app.chemical_properties cp;



select distinct name,unit,source from ccd_app.chemical_properties cp
-- where name like 'Log%'
order by name,unit;


select * from ccd_app.chemical_properties cp
where name='LogD5.5'
order by dtxsid,unit ;


select * from ccd_app.chemical_properties cp
where dtxsid='DTXSID00163760';
1111
