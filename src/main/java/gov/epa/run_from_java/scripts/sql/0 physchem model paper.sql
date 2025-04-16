-- get sources used for each dataset
select p.name,
       case when ps.name is not null then ps.name else ls.name end as source_name,
       count(dpc.id)
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
where d.id = did.fk_datasets_id and keep = true
-- where d.name in ('HLC v1 modeling', 'VP v1 modeling','BP v1 modeling','WS v1 modeling','LogP v1 modeling','MP v1 modeling') and keep = true
group by source_name, p.name
order by p.name, source_name;


-- Get dpc counts (DSStox mapped)
select p.name, count(dpc.id)
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
where d.name in ('HLC v1 modeling', 'VP v1 modeling','BP v1 modeling','WS v1 modeling','LogP v1 modeling','MP v1 modeling')
group by p.name
order by p.name;

-- Get datapoint counts using datasets_in_cheminformatics_modules:
select p.name,count(dp.id)
from qsar_datasets.data_points dp
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
where d.id = did.fk_datasets_id
group by p.name
order by p.name;


-- Get datapoint counts using dataset names:
select p.name,count(dp.id)
from qsar_datasets.data_points dp
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
where d.name in ('HLC v1 modeling', 'VP v1 modeling','BP v1 modeling','WS v1 modeling','LogP v1 modeling','MP v1 modeling')
group by p.name
order by p.name;




--get smiles where descriptors are missing:
select distinct  dp.canon_qsar_smiles
from qsar_datasets.data_points dp
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
        join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
        join qsar_descriptors.descriptor_sets ds  on dv.fk_descriptor_set_id = ds.id
where d.name in ('HLC v1 modeling', 'VP v1 modeling','BP v1 modeling','WS v1 modeling','LogP v1 modeling','MP v1 modeling')
  and ds.name='WebTEST-default' and dv.values_tsv is null;


-- get embedding for HLC model
select embedding_tsv,m2.name from qsar_models.models m
join qsar_datasets.datasets d on m.dataset_name=d.name
join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
join qsar_models.methods m2 on m.fk_method_id = m2.id
where d.name='HLC v1 modeling' and m.splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;