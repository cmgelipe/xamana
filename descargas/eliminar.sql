create or replace function eliminar(original varchar) RETURNS int as
$$
declare
	nTablas record;
	contador int;
	consulta text;
	dblinkExt bigint;
begin
	dblinkExt=(select count(extname) from pg_extension where extname='dblink');
	if dblinkExt=0 then
		CREATE EXTENSION dblink;
	end if;
	for nTablas in SELECT table_name FROM information_schema.tables 
					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like 'tabla1_%'
		loop
			consulta='alter table '|| nTablas.table_name ||' drop column '||original;
			perform dblink_exec('dbname=Prueba1 user=postgres password=sunny_4teamo',consulta);
		end loop;
	return 1;
end;
$$ language plpgsql;