--SELECT t0.column_name, t0.data_type FROM information_schema.columns as t0 left join (SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name='tabla1_f1') as t1 on t0.column_name=t1.column_name WHERE table_schema = 'public' AND table_name='tabla1'
--alter table tabla1 drop column otra
--SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name='tabla1_f1'
--alter table tabla1 drop column otra
--select t0.column_name, t0.data_type from information_schema.columns as t0 left join (SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name='tabla1') as t1 on t0.column_name=t1.column_name where table_schema = 'public' AND table_name='tabla1_f1' and t1 is null
--alter table tabla1 rename column tamanio to tam
--SELECT t0.column_name, t0.data_type FROM information_schema.columns as t0 left join (SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name='tabla1_f1') as t1 on t0.column_name=t1.column_name WHERE table_schema = 'public' AND table_name='tabla1'
--select t0.column_name, t0.data_type --, t1.column_name, t1.data_type 
--FROM information_schema.columns as t0 
--where table_schema = 'public' AND table_name='tabla1'
--and t0.column_name not in (SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name='tabla1_f1')
--alter table tabla1 rename column tam to tama
--alter table tabla1 alter column tama set data type varchar;

--alter table tabla1 add column otra varchar;
--alter table tabla1 drop column otra;
alter table tabla1 alter column tamanio set data type text;