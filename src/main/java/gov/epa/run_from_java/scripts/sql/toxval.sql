select distinct author, year from bcfbaf b
inner join chemical c on c.dtxsid=b.dtxsid
left join species s on s.species_common=b.species_common
where s.species_supercategory like '%fish%' and logbcf is not null and tissue='Whole body'
order by author;

# BCF whole body
select distinct b.bcfbaf_id, casrn,c.name, b.dtxsid, logbcf, units, tissue, calc_method, comments, b.water_conc, exposure_type, exposure_duration,  media, temperature, pH, b.species_common, author,title, b.year,b.journal from bcfbaf b
join chemical c on c.dtxsid=b.dtxsid
join species s on s.species_common=b.species_common
where s.species_supercategory like '%fish%' and b.logbcf is not null and b.tissue='Whole body'
# and c.dtxsid='DTXSID3025382'
# and logbcf>3.1227 and logbcf< 3.12276
order by dtxsid, logbcf;

select b.bcfbaf_id, casrn,c.name, b.dtxsid, logbcf, units, tissue, calc_method, comments, b.water_conc, exposure_type, exposure_duration,  media, temperature, pH, b.species_common, author,title, b.year,b.journal from bcfbaf b
# select distinct species_common from bcfbaf b
join chemical c on c.dtxsid=b.dtxsid
where logbcf is not null;





select distinct (c.dtxsid) from bcfbaf b
inner join chemical c on c.dtxsid=b.dtxsid
left join species s on s.species_common=b.species_common
where s.species_supercategory like '%fish%' and logbcf is not null and tissue='Whole body';









select species.species_common, species_supercategory from species
where species_common like '%Medaka%'

# select * from bcfbaf where dtxsid='DTXSID7021106'
# and logbcf>3.12 and logbcf<3.3



select count(bcfbaf_hash) from bcfbaf b
join species s on s.species_common=b.species_common
join chemical c on c.dtxsid=b.dtxsid
where s.species_supercategory like '%fish%' and logbcf is not null and tissue='Whole body'


select distinct c.casrn,c.name,c.dtxsid from bcfbaf b
# join species s on s.species_common=b.species_common
join chemical c on c.dtxsid=b.dtxsid
# where casrn='50-29-3';
# where s.species_supercategory like '%fish%' and logbcf is not null and tissue='Whole body'
