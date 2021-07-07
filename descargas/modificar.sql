create or replace function modificar(original varchar,modo int) RETURNS int as
$$
declare
	nTablas record;
	contador int;
	res2 varchar;
	res3 varchar;
	consulta text;
	dblinkExt bigint;
begin
	dblinkExt=(select count(extname) from pg_extension where extname='dblink');
	if dblinkExt=0 then
		CREATE EXTENSION dblink;
	end if;
	res2=(select split_part(original,'::',1));
	res3=(select split_part(original,'::',2));
	for nTablas in SELECT table_name FROM information_schema.tables 
					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like 'tabla1_%'
		loop
			if modo=1 then
				consulta='alter table '|| nTablas.table_name ||' rename column '||res2||' to '||res3;
				perform dblink_exec('dbname=Prueba1 user=postgres password=sunny_4teamo',consulta);
			end if;
			if modo=2 then
				consulta='alter table '|| nTablas.table_name ||' alter column '||res2||' set data type '||res3;
				perform dblink_exec('dbname=Prueba1 user=postgres password=sunny_4teamo',consulta);
			end if;
		end loop;
	raise notice '%-%',res2,res3;
	return 1;
end;
$$ language plpgsql;