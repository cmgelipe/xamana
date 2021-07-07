
CREATE or replace FUNCTION public.trg_alter_test()
    RETURNS event_trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
DECLARE
     obj record;
	 nActuales record;
	 nTablas name;
	 cActuales bigint;
	 cOtras bigint;
	 nombreAtributo name;
	 nombreAtributoFragmento name;
	 faltante record;
	 prueba record;
	 bandera boolean;
	 consulta text;
	 consultaRemota text;
	 dblinkExt bigint;
BEGIN
	dblinkExt=(select count(extname) from pg_extension where extname='dblink');
	if dblinkExt=0 then
		CREATE EXTENSION dblink;
	end if;
    FOR obj in SELECT * FROM pg_event_trigger_ddl_commands()
         LOOP
              IF obj.objid = 'tabla1'::regclass THEN --tabla1 es nombre de la tabla que se fragmentó
			  	cActuales=(SELECT count(column_name) FROM information_schema.columns 
						   WHERE table_schema = 'public' AND table_name='tabla1');
				bandera=false;
                for nTablas in SELECT table_name FROM information_schema.tables 
					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like 'tabla1_%'
					LOOP
					cOtras=(SELECT count(column_name) FROM information_schema.columns 
						   WHERE table_schema = 'public' AND table_name=nTablas);
					IF cActuales>cOtras then
						for faltante in SELECT t0.column_name, t0.data_type 
						FROM information_schema.columns as t0
						left join (SELECT column_name 
	  						FROM information_schema.columns 
							WHERE table_schema = 'public' 
	  						AND table_name=nTablas) as t1
						on t0.column_name=t1.column_name
						WHERE table_schema = 'public' 
						AND table_name='tabla1'
						and t1 is null
						loop
							begin
								consulta='alter table '|| nTablas ||' add column '||faltante.column_name||' '||faltante.data_type;
								perform dblink_exec('dbname=Prueba user=postgres password=sunny_4teamo',consulta);--Agregar ip local
								if bandera=false then
									consultaRemota='select crear('''||faltante.column_name||'::'||faltante.data_type||''')';	
									--perform dblink('dbname=Prueba1 user=postgres password=sunny_4teamo',consultaRemota) as tt;--agregar cada ip remota
									select crear into prueba from dblink('dbname=Prueba1 user=postgres password=sunny_4teamo',consultaRemota) as t1(crear int);--agregar cada ip remota
									bandera=true;
								end if;
								RAISE NOTICE '% %',faltante.column_name, faltante.data_type;
							end;
						end loop;
					end if;
						   RAISE NOTICE '% %',cOtras, nTablas::text;
					END LOOP;
				END IF;
         END LOOP;
END
$BODY$;

CREATE EVENT TRIGGER test_trigger ON ddl_command_end WHEN TAG IN ('ALTER TABLE')
EXECUTE FUNCTION trg_alter_test();
