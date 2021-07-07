create or replace function crear(original varchar) RETURNS int as
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
			consulta='alter table '|| nTablas.table_name ||' add column '||res2||' '||res3;
			raise notice '%',consulta;
			perform dblink_exec('dbname=Prueba1 user=postgres password=sunny_4teamo',consulta);
		end loop;
	raise notice '%-%',res2,res3;
	return 1;
end;
$$ language plpgsql;