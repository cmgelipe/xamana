CREATE FUNCTION public.trg_alter_test_modificar()
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
	 attOriginal record;
	 attOtro record;
	 consulta text;
	 con1 int;
	 con2 int;
	 prueba record;
	 consultaRemota text;
	 bandera boolean;
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
					IF cActuales=cOtras then
					con1=0;
						for attOriginal in select t0.column_name, t0.data_type
											from information_schema.columns as t0 
											where table_schema = 'public' AND table_name='tabla1' 
						loop
							con1=con1+1;
							con2=0;
							for attOtro in select t0.column_name, t0.data_type
											from information_schema.columns as t0 
											where table_schema = 'public' AND table_name=nTablas
							loop
								con2=con2+1;
								if con2=con1 then
									if attOtro.column_name<>attOriginal.column_name then 
										--RAISE NOTICE 'DIFERENTE % %',attOtro.column_name, attOriginal.column_name;
										consulta='alter table '|| nTablas ||' rename column '||attOtro.column_name||' to '||attOriginal.column_name;
										perform dblink_exec('dbname=Prueba user=postgres password=sunny_4teamo',consulta);	
										if bandera=false then
											consultaRemota='select modificar('''||attOtro.column_name||'::'||attOriginal.column_name||''',1)';
											select modificar into prueba from dblink('dbname=Prueba1 user=postgres password=sunny_4teamo',consultaRemota) as t1(modificar int);--agregar cada ip remota
											bandera=true;
										end if;
									end if;
									if attOtro.data_type<>attOriginal.data_type then
										RAISE NOTICE 'DIFERENTE % %',attOtro.data_type, attOriginal.data_type;
										consulta='alter table '|| nTablas||' alter column '||attOtro.column_name||' set data type '||attOriginal.data_type;
										perform dblink_exec('dbname=Prueba user=postgres password=sunny_4teamo',consulta);		
										if bandera=false then
											consultaRemota='select modificar('''||attOtro.column_name||'::'||attOriginal.data_type||''',2)';
											select modificar into prueba from dblink('dbname=Prueba1 user=postgres password=sunny_4teamo',consultaRemota) as t1(modificar int);--agregar cada ip remota
											bandera=true;
										end if;
									end if;
								end if;
							end loop;
						end loop;
					end if;
					END LOOP;
				END IF;
         END LOOP;
END
$BODY$;

CREATE EVENT TRIGGER test_trigger_modificar ON ddl_command_end WHEN TAG IN ('ALTER TABLE')
EXECUTE FUNCTION trg_alter_test_modificar();