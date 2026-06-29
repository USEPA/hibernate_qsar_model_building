select distinct
    	ss.id,
       ss.dsstox_record_id as dsstoxRecordId,
			ss.external_id as externalId,
			ssi.identifier as sourceName,
			ssi2.identifier as sourceSmiles,
			sgsm.connection_reason as connectionReason,
			sgsm.linkage_score as linkageScore,
			sgsm.curator_validated as curatorValidated,
			gs.dsstox_substance_id as dsstoxSubstanceId,
			c.dsstox_compound_id as dsstoxCompoundId,
			gs.casrn as casrn,
			gs.preferred_name as preferredName,
			gs.substance_type as substanceType,
			c.smiles as smiles,
			c.mol_weight as molWeight,
			c2.smiles as qsarReadySmiles
			from source_substances ss
			left join source_generic_substance_mappings sgsm on ss.id = sgsm.fk_source_substance_id
			left join generic_substances gs on sgsm.fk_generic_substance_id = gs.id
			left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
			left join compounds c on gsc.fk_compound_id = c.id
			left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor and cr.fk_compound_relationship_type_id=1
			left join compounds c2 on cr.fk_compound_id_successor=c2.id
			left join source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id and ssi.identifier_type='NAME'
			left join source_substance_identifiers ssi2 on ss.id = ssi2.fk_source_substance_id and ssi2.identifier_type='STRUCTURE'
	        join chemical_lists cl on ss.fk_chemical_list_id = cl.id
			where cl.name = 'PubCHEMLITE112024'
	order by ss.id desc;


select count(distinct (ss.id))
			from source_substances ss
			left join source_generic_substance_mappings sgsm on ss.id = sgsm.fk_source_substance_id
			left join generic_substances gs on sgsm.fk_generic_substance_id = gs.id
			left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
			left join compounds c on gsc.fk_compound_id = c.id
			left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor and cr.fk_compound_relationship_type_id=1
			left join compounds c2 on cr.fk_compound_id_successor=c2.id
			left join source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id and ssi.identifier_type='NAME'
			left join source_substance_identifiers ssi2 on ss.id = ssi2.fk_source_substance_id and ssi2.identifier_type='STRUCTURE'
	        join chemical_lists cl on ss.fk_chemical_list_id = cl.id
			where cl.name = 'PubCHEMLITE112024';
