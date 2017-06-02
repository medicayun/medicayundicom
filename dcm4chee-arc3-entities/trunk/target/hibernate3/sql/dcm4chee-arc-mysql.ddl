
    create table ae (
        pk bigint not null auto_increment,
        ae_group varchar(255),
        cipher_suites varchar(255),
        department varchar(255),
        ae_desc varchar(255),
        fs_group_id varchar(255),
        hostname varchar(255) not null,
        installed bit,
        institution varchar(255),
        acc_no_issuer varchar(255),
        pat_id_issuer varchar(255),
        passwd varchar(255),
        port integer not null,
        station_name varchar(255),
        aet varchar(255) not null,
        user_id varchar(255),
        wado_url varchar(255),
        primary key (pk)
    ) type=InnoDB;

    create table code (
        pk bigint not null auto_increment,
        code_meaning varchar(255),
        code_value varchar(255) not null,
        code_designator varchar(255) not null,
        code_version varchar(255),
        primary key (pk)
    ) type=InnoDB;

    create table content_item (
        pk bigint not null auto_increment,
        rel_type varchar(255),
        text_value varchar(255),
        code_fk bigint,
        name_fk bigint,
        instance_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table device (
        pk bigint not null auto_increment,
        modality varchar(255) not null,
        station_aet varchar(255) not null,
        station_name varchar(255) not null,
        primary key (pk)
    ) type=InnoDB;

    create table files (
        pk bigint not null auto_increment,
        created_time datetime,
        filepath varchar(255),
        file_size bigint,
        file_status integer,
        file_md5 varchar(255),
        md5_check_time datetime,
        file_tsuid varchar(255),
        filesystem_fk bigint,
        instance_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table filesystem (
        pk bigint not null auto_increment,
        availability integer not null,
        dirpath varchar(255) not null unique,
        fs_group_id varchar(255) not null,
        retrieve_aet varchar(255) not null,
        fs_status integer not null,
        user_info varchar(255),
        next_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table gppps (
        pk bigint not null auto_increment,
        created_time datetime,
        pps_attrs longblob,
        pps_iuid varchar(255) not null,
        pps_start datetime,
        pps_status integer not null,
        updated_time datetime,
        patient_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table gpsps (
        pk bigint not null auto_increment,
        item_attrs longblob,
        end_datetime datetime,
        in_availability integer,
        gpsps_prior integer,
        gpsps_iuid varchar(255) not null,
        start_datetime datetime not null,
        gpsps_status integer,
        gpsps_tuid varchar(255),
        patient_fk bigint,
        code_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table gpsps_perf (
        pk bigint not null auto_increment,
        hum_perf_fn_sx varchar(255),
        hum_perf_gn_sx varchar(255),
        hum_perf_i_name varchar(255),
        human_perf_name varchar(255),
        hum_perf_p_name varchar(255),
        code_fk bigint,
        gpsps_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table gpsps_req (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        req_proc_id varchar(255),
        gpsps_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table hp (
        pk bigint not null auto_increment,
        hp_attrs longblob,
        hp_level integer,
        hp_name varchar(255),
        num_priors integer,
        num_screens integer,
        hp_cuid varchar(255),
        hp_iuid varchar(255) not null,
        hp_group varchar(255),
        user_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table hpdef (
        pk bigint not null auto_increment,
        laterality varchar(255),
        modality varchar(255),
        hp_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table instance (
        pk bigint not null auto_increment,
        all_attrs bit not null,
        archived bit not null,
        availability integer not null,
        sr_complete varchar(255),
        content_datetime datetime,
        created_time datetime,
        inst_attrs longblob,
        ext_retr_aet varchar(255),
        inst_custom1 varchar(255),
        inst_custom2 varchar(255),
        inst_custom3 varchar(255),
        inst_no varchar(255),
        retrieve_aets varchar(255),
        sop_cuid varchar(255) not null,
        sop_iuid varchar(255) not null,
        commitment bit not null,
        inst_status integer not null,
        updated_time datetime,
        sr_verified varchar(255),
        srcode_fk bigint,
        media_fk bigint,
        series_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table issuer (
        pk bigint not null auto_increment,
        entity_id varchar(255),
        entity_uid varchar(255),
        entity_uid_type varchar(255),
        primary key (pk)
    ) type=InnoDB;

    create table media (
        pk bigint not null auto_increment,
        created_time datetime,
        fileset_id varchar(255),
        fileset_iuid varchar(255) not null,
        media_rq_iuid varchar(255),
        media_status integer not null,
        media_status_info varchar(255),
        media_usage bigint not null,
        updated_time datetime,
        primary key (pk)
    ) type=InnoDB;

    create table mpps (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        created_time datetime,
        mpps_attrs longblob,
        modality varchar(255),
        station_aet varchar(255),
        mpps_iuid varchar(255) not null unique,
        pps_start datetime,
        mpps_status integer not null,
        updated_time datetime,
        drcode_fk bigint,
        patient_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table mwl_item (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        created_time datetime,
        item_attrs longblob,
        modality varchar(255) not null,
        req_proc_id varchar(255) not null,
        perf_phys_fn_sx varchar(255),
        perf_phys_gn_sx varchar(255),
        perf_phys_i_name varchar(255),
        perf_physician varchar(255),
        perf_phys_p_name varchar(255),
        sps_id varchar(255),
        station_aet varchar(255) not null,
        station_name varchar(255),
        start_datetime datetime not null,
        sps_status integer,
        study_iuid varchar(255) not null,
        updated_time datetime,
        patient_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table other_pid (
        pk bigint not null auto_increment,
        pat_id_issuer varchar(255) not null,
        pat_id varchar(255) not null,
        primary key (pk)
    ) type=InnoDB;

    create table patient (
        pk bigint not null auto_increment,
        created_time datetime,
        pat_attrs longblob,
        pat_id_issuer varchar(255),
        pat_birthdate varchar(255),
        pat_custom1 varchar(255),
        pat_custom2 varchar(255),
        pat_custom3 varchar(255),
        pat_fn_sx varchar(255),
        pat_gn_sx varchar(255),
        pat_id varchar(255),
        pat_i_name varchar(255),
        pat_name varchar(255),
        pat_p_name varchar(255),
        pat_sex varchar(255),
        updated_time datetime,
        merge_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table priv_file (
        pk bigint not null auto_increment,
        file_md5 varchar(255),
        filepath varchar(255) not null,
        file_size bigint,
        file_status integer,
        file_tsuid varchar(255) not null,
        filesystem_fk bigint,
        instance_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table priv_instance (
        pk bigint not null auto_increment,
        created_time datetime,
        inst_attrs longblob,
        priv_type integer not null,
        sop_iuid varchar(255) not null,
        series_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table priv_patient (
        pk bigint not null auto_increment,
        pat_attrs longblob,
        pat_id_issuer varchar(255),
        pat_id varchar(255),
        pat_name varchar(255),
        priv_type integer not null,
        primary key (pk)
    ) type=InnoDB;

    create table priv_series (
        pk bigint not null auto_increment,
        series_attrs longblob,
        priv_type integer not null,
        series_iuid varchar(255) not null,
        src_aet varchar(255),
        study_fk bigint not null,
        primary key (pk)
    ) type=InnoDB;

    create table priv_study (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        study_attrs longblob,
        priv_type integer not null,
        study_iuid varchar(255) not null,
        patient_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table published_study (
        pk bigint not null auto_increment,
        created_time datetime,
        docentry_uid varchar(255),
        doc_uid varchar(255),
        repository_uid varchar(255),
        status integer not null,
        updated_time datetime,
        study_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table rel_dev_proto (
        prcode_fk bigint not null,
        device_fk bigint not null,
        primary key (prcode_fk, device_fk)
    ) type=InnoDB;

    create table rel_gpsps_appcode (
        appcode_fk bigint not null,
        gpsps_fk bigint not null,
        primary key (appcode_fk, gpsps_fk)
    ) type=InnoDB;

    create table rel_gpsps_devclass (
        devclass_fk bigint not null,
        gpsps_fk bigint not null,
        primary key (devclass_fk, gpsps_fk)
    ) type=InnoDB;

    create table rel_gpsps_devloc (
        devloc_fk bigint not null,
        gpsps_fk bigint not null,
        primary key (devloc_fk, gpsps_fk)
    ) type=InnoDB;

    create table rel_gpsps_devname (
        devname_fk bigint not null,
        gpsps_fk bigint not null,
        primary key (devname_fk, gpsps_fk)
    ) type=InnoDB;

    create table rel_gpsps_gppps (
        gppps_fk bigint not null,
        gpsps_fk bigint not null,
        primary key (gppps_fk, gpsps_fk)
    ) type=InnoDB;

    create table rel_hpdef_proc (
        hpdef_fk bigint not null,
        proc_fk bigint not null,
        primary key (hpdef_fk, proc_fk)
    ) type=InnoDB;

    create table rel_hpdef_reason (
        hpdef_fk bigint not null,
        reason_fk bigint not null,
        primary key (hpdef_fk, reason_fk)
    ) type=InnoDB;

    create table rel_hpdef_region (
        hpdef_fk bigint not null,
        region_fk bigint not null,
        primary key (hpdef_fk, region_fk)
    ) type=InnoDB;

    create table rel_pat_other_pid (
        patient_fk bigint not null,
        other_pid_fk bigint not null,
        primary key (other_pid_fk, patient_fk)
    ) type=InnoDB;

    create table rel_study_pcode (
        study_fk bigint not null,
        pcode_fk bigint not null,
        primary key (study_fk, pcode_fk)
    ) type=InnoDB;

    create table rel_ups_appcode (
        ups_fk bigint not null,
        appcode_fk bigint not null,
        primary key (ups_fk, appcode_fk)
    ) type=InnoDB;

    create table rel_ups_devclass (
        ups_fk bigint not null,
        devclass_fk bigint not null,
        primary key (ups_fk, devclass_fk)
    ) type=InnoDB;

    create table rel_ups_devloc (
        ups_fk bigint not null,
        devloc_fk bigint not null,
        primary key (ups_fk, devloc_fk)
    ) type=InnoDB;

    create table rel_ups_devname (
        ups_fk bigint not null,
        devname_fk bigint not null,
        primary key (ups_fk, devname_fk)
    ) type=InnoDB;

    create table rel_ups_performer (
        ups_fk bigint not null,
        performer_fk bigint not null,
        primary key (ups_fk, performer_fk)
    ) type=InnoDB;

    create table series (
        pk bigint not null auto_increment,
        availability integer not null,
        body_part varchar(255),
        created_time datetime,
        series_attrs longblob,
        ext_retr_aet varchar(255),
        fileset_id varchar(255),
        fileset_iuid varchar(255),
        institution varchar(255),
        department varchar(255),
        laterality varchar(255),
        modality varchar(255),
        num_instances integer not null,
        pps_iuid varchar(255),
        pps_start datetime,
        perf_phys_fn_sx varchar(255),
        perf_phys_gn_sx varchar(255),
        perf_phys_i_name varchar(255),
        perf_physician varchar(255),
        perf_phys_p_name varchar(255),
        retrieve_aets varchar(255),
        series_custom1 varchar(255),
        series_custom2 varchar(255),
        series_custom3 varchar(255),
        series_desc varchar(255),
        series_iuid varchar(255) not null,
        series_no varchar(255),
        src_aet varchar(255),
        station_name varchar(255),
        series_status integer not null,
        updated_time datetime,
        inst_code_fk bigint,
        mpps_fk bigint,
        study_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table series_req (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        req_proc_id varchar(255),
        req_physician varchar(255),
        req_phys_fn_sx varchar(255),
        req_phys_gn_sx varchar(255),
        req_phys_i_name varchar(255),
        req_phys_p_name varchar(255),
        req_service varchar(255),
        sps_id varchar(255),
        study_iuid varchar(255),
        accno_issuer_fk bigint,
        series_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table study (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        availability integer not null,
        created_time datetime,
        study_attrs longblob,
        ext_retr_aet varchar(255),
        fileset_id varchar(255),
        fileset_iuid varchar(255),
        mods_in_study varchar(255),
        num_instances integer not null,
        num_series integer not null,
        patient_fk bigint,
        ref_phys_fn_sx varchar(255),
        ref_phys_gn_sx varchar(255),
        ref_phys_i_name varchar(255),
        ref_physician varchar(255),
        ref_phys_p_name varchar(255),
        retrieve_aets varchar(255),
        cuids_in_study varchar(255),
        study_custom1 varchar(255),
        study_custom2 varchar(255),
        study_custom3 varchar(255),
        study_datetime datetime,
        study_desc varchar(255),
        study_id varchar(255),
        study_iuid varchar(255) not null,
        study_status integer not null,
        study_status_id varchar(255),
        checked_time datetime,
        updated_time datetime,
        accno_issuer_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table study_on_fs (
        pk bigint not null auto_increment,
        access_time datetime not null,
        mark_to_delete bit not null,
        filesystem_fk bigint,
        study_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table study_permission (
        pk bigint not null auto_increment,
        action varchar(255) not null,
        roles varchar(255) not null,
        study_iuid varchar(255) not null,
        primary key (pk)
    ) type=InnoDB;

    create table ups (
        pk bigint not null auto_increment,
        adm_id varchar(255),
        created_time datetime,
        ups_attrs longblob,
        ups_compl_time datetime,
        adm_id_issuer_id varchar(255),
        adm_id_issuer_uid varchar(255),
        ups_prior integer,
        ups_label varchar(255) not null,
        ups_start_time datetime not null,
        ups_iuid varchar(255) not null,
        ups_state integer,
        ups_tuid varchar(255),
        updated_time datetime,
        uwl_label varchar(255) not null,
        patient_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table ups_rel_ps (
        pk bigint not null auto_increment,
        sop_cuid varchar(255),
        sop_iuid varchar(255),
        ups_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table ups_repl_ps (
        pk bigint not null auto_increment,
        sop_cuid varchar(255),
        sop_iuid varchar(255),
        ups_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table ups_req (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        confidentiality varchar(255),
        req_proc_id varchar(255),
        req_service varchar(255),
        ups_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table ups_subscr (
        pk bigint not null auto_increment,
        aet varchar(255),
        deletion_lock bit,
        ups_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    create table verify_observer (
        pk bigint not null auto_increment,
        verify_datetime datetime,
        observer_fn_sx varchar(255),
        observer_gn_sx varchar(255),
        observer_i_name varchar(255),
        observer_name varchar(255),
        observer_p_name varchar(255),
        instance_fk bigint,
        primary key (pk)
    ) type=InnoDB;

    alter table content_item 
        add index FK318FE31937EDB1AA (instance_fk), 
        add constraint FK318FE31937EDB1AA 
        foreign key (instance_fk) 
        references instance (pk);

    alter table content_item 
        add index FK318FE31970C135AA (code_fk), 
        add constraint FK318FE31970C135AA 
        foreign key (code_fk) 
        references code (pk);

    alter table content_item 
        add index FK318FE3199F40BC4C (name_fk), 
        add constraint FK318FE3199F40BC4C 
        foreign key (name_fk) 
        references code (pk);

    alter table files 
        add index FK5CEBA7737EDB1AA (instance_fk), 
        add constraint FK5CEBA7737EDB1AA 
        foreign key (instance_fk) 
        references instance (pk);

    alter table files 
        add index FK5CEBA77206F5C8A (filesystem_fk), 
        add constraint FK5CEBA77206F5C8A 
        foreign key (filesystem_fk) 
        references filesystem (pk);

    alter table filesystem 
        add index FKA2455AABE9B3E742 (next_fk), 
        add constraint FKA2455AABE9B3E742 
        foreign key (next_fk) 
        references filesystem (pk);

    alter table gppps 
        add index FK5E010EAA511AE1E (patient_fk), 
        add constraint FK5E010EAA511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    alter table gpsps 
        add index FK5E01C2D70C135AA (code_fk), 
        add constraint FK5E01C2D70C135AA 
        foreign key (code_fk) 
        references code (pk);

    alter table gpsps 
        add index FK5E01C2DA511AE1E (patient_fk), 
        add constraint FK5E01C2DA511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    alter table gpsps_perf 
        add index FKC00AA3BB9637239E (gpsps_fk), 
        add constraint FKC00AA3BB9637239E 
        foreign key (gpsps_fk) 
        references gpsps (pk);

    alter table gpsps_perf 
        add index FKC00AA3BB70C135AA (code_fk), 
        add constraint FKC00AA3BB70C135AA 
        foreign key (code_fk) 
        references code (pk);

    alter table gpsps_req 
        add index FKA319258C9637239E (gpsps_fk), 
        add constraint FKA319258C9637239E 
        foreign key (gpsps_fk) 
        references gpsps (pk);

    alter table hp 
        add index FKD082FD8C30C (user_fk), 
        add constraint FKD082FD8C30C 
        foreign key (user_fk) 
        references code (pk);

    alter table hpdef 
        add index FK5EDF9FDC7D4B11E (hp_fk), 
        add constraint FK5EDF9FDC7D4B11E 
        foreign key (hp_fk) 
        references hp (pk);

    alter table instance 
        add index FK211694958151AFEA (series_fk), 
        add constraint FK211694958151AFEA 
        foreign key (series_fk) 
        references series (pk);

    alter table instance 
        add index FK21169495A5F47C3E (media_fk), 
        add constraint FK21169495A5F47C3E 
        foreign key (media_fk) 
        references media (pk);

    alter table instance 
        add index FK211694954DC50E6B (srcode_fk), 
        add constraint FK211694954DC50E6B 
        foreign key (srcode_fk) 
        references code (pk);

    alter table mpps 
        add index FK333EE69DC28D5C (drcode_fk), 
        add constraint FK333EE69DC28D5C 
        foreign key (drcode_fk) 
        references code (pk);

    alter table mpps 
        add index FK333EE6A511AE1E (patient_fk), 
        add constraint FK333EE6A511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    alter table mwl_item 
        add index FK8F9D3D30A511AE1E (patient_fk), 
        add constraint FK8F9D3D30A511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    alter table patient 
        add index FKD0D3EB05206840B (merge_fk), 
        add constraint FKD0D3EB05206840B 
        foreign key (merge_fk) 
        references patient (pk);

    alter table priv_file 
        add index FKA16B610C242260C1 (instance_fk), 
        add constraint FKA16B610C242260C1 
        foreign key (instance_fk) 
        references priv_instance (pk);

    alter table priv_file 
        add index FKA16B610C206F5C8A (filesystem_fk), 
        add constraint FKA16B610C206F5C8A 
        foreign key (filesystem_fk) 
        references filesystem (pk);

    alter table priv_instance 
        add index FK9891B8255C4538C1 (series_fk), 
        add constraint FK9891B8255C4538C1 
        foreign key (series_fk) 
        references priv_series (pk);

    alter table priv_series 
        add index FKA10C6078477F667 (study_fk), 
        add constraint FKA10C6078477F667 
        foreign key (study_fk) 
        references priv_study (pk);

    alter table priv_study 
        add index FK8CBD1439288F4027 (patient_fk), 
        add constraint FK8CBD1439288F4027 
        foreign key (patient_fk) 
        references priv_patient (pk);

    alter table published_study 
        add index FK707B68384BDB761E (study_fk), 
        add constraint FK707B68384BDB761E 
        foreign key (study_fk) 
        references study (pk);

    alter table rel_dev_proto 
        add index FKA8591518E207F051 (prcode_fk), 
        add constraint FKA8591518E207F051 
        foreign key (prcode_fk) 
        references device (pk);

    alter table rel_dev_proto 
        add index FKA85915183A1E80A1 (device_fk), 
        add constraint FKA85915183A1E80A1 
        foreign key (device_fk) 
        references code (pk);

    alter table rel_gpsps_appcode 
        add index FKB35F8056F3B2489D (appcode_fk), 
        add constraint FKB35F8056F3B2489D 
        foreign key (appcode_fk) 
        references gpsps (pk);

    alter table rel_gpsps_appcode 
        add index FKB35F8056F38E1F6A (gpsps_fk), 
        add constraint FKB35F8056F38E1F6A 
        foreign key (gpsps_fk) 
        references code (pk);

    alter table rel_gpsps_devclass 
        add index FKB48B32BBE6672D88 (devclass_fk), 
        add constraint FKB48B32BBE6672D88 
        foreign key (devclass_fk) 
        references gpsps (pk);

    alter table rel_gpsps_devclass 
        add index FKB48B32BBF38E1F6A (gpsps_fk), 
        add constraint FKB48B32BBF38E1F6A 
        foreign key (gpsps_fk) 
        references code (pk);

    alter table rel_gpsps_devloc 
        add index FK862EA143E28E4000 (devloc_fk), 
        add constraint FK862EA143E28E4000 
        foreign key (devloc_fk) 
        references gpsps (pk);

    alter table rel_gpsps_devloc 
        add index FK862EA143F38E1F6A (gpsps_fk), 
        add constraint FK862EA143F38E1F6A 
        foreign key (gpsps_fk) 
        references code (pk);

    alter table rel_gpsps_devname 
        add index FK3FA63CE8F646ACB (devname_fk), 
        add constraint FK3FA63CE8F646ACB 
        foreign key (devname_fk) 
        references gpsps (pk);

    alter table rel_gpsps_devname 
        add index FK3FA63CE8F38E1F6A (gpsps_fk), 
        add constraint FK3FA63CE8F38E1F6A 
        foreign key (gpsps_fk) 
        references code (pk);

    alter table rel_gpsps_gppps 
        add index FKDB38F3B291188E7E (gppps_fk), 
        add constraint FKDB38F3B291188E7E 
        foreign key (gppps_fk) 
        references gppps (pk);

    alter table rel_gpsps_gppps 
        add index FKDB38F3B29637239E (gpsps_fk), 
        add constraint FKDB38F3B29637239E 
        foreign key (gpsps_fk) 
        references gpsps (pk);

    alter table rel_hpdef_proc 
        add index FK70548BBE262ADBA1 (proc_fk), 
        add constraint FK70548BBE262ADBA1 
        foreign key (proc_fk) 
        references code (pk);

    alter table rel_hpdef_proc 
        add index FK70548BBEC4325808 (hpdef_fk), 
        add constraint FK70548BBEC4325808 
        foreign key (hpdef_fk) 
        references hpdef (pk);

    alter table rel_hpdef_reason 
        add index FKB00D00ACC4325808 (hpdef_fk), 
        add constraint FKB00D00ACC4325808 
        foreign key (hpdef_fk) 
        references hpdef (pk);

    alter table rel_hpdef_reason 
        add index FKB00D00AC10B75DF3 (reason_fk), 
        add constraint FKB00D00AC10B75DF3 
        foreign key (reason_fk) 
        references code (pk);

    alter table rel_hpdef_region 
        add index FKB00F955C3D0C4B43 (region_fk), 
        add constraint FKB00F955C3D0C4B43 
        foreign key (region_fk) 
        references code (pk);

    alter table rel_hpdef_region 
        add index FKB00F955CC4325808 (hpdef_fk), 
        add constraint FKB00F955CC4325808 
        foreign key (hpdef_fk) 
        references hpdef (pk);

    alter table rel_pat_other_pid 
        add index FK929DDE1A23ED359E (other_pid_fk), 
        add constraint FK929DDE1A23ED359E 
        foreign key (other_pid_fk) 
        references other_pid (pk);

    alter table rel_pat_other_pid 
        add index FK929DDE1AA511AE1E (patient_fk), 
        add constraint FK929DDE1AA511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    alter table rel_study_pcode 
        add index FK2EF025C1E344D73A (pcode_fk), 
        add constraint FK2EF025C1E344D73A 
        foreign key (pcode_fk) 
        references code (pk);

    alter table rel_study_pcode 
        add index FK2EF025C14BDB761E (study_fk), 
        add constraint FK2EF025C14BDB761E 
        foreign key (study_fk) 
        references study (pk);

    alter table rel_ups_appcode 
        add index FK7F11DCE151094469 (appcode_fk), 
        add constraint FK7F11DCE151094469 
        foreign key (appcode_fk) 
        references code (pk);

    alter table rel_ups_appcode 
        add index FK7F11DCE17504163E (ups_fk), 
        add constraint FK7F11DCE17504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table rel_ups_devclass 
        add index FK5F24679043BE2954 (devclass_fk), 
        add constraint FK5F24679043BE2954 
        foreign key (devclass_fk) 
        references code (pk);

    alter table rel_ups_devclass 
        add index FK5F2467907504163E (ups_fk), 
        add constraint FK5F2467907504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table rel_ups_devloc 
        add index FKADC907583FE53BCC (devloc_fk), 
        add constraint FKADC907583FE53BCC 
        foreign key (devloc_fk) 
        references code (pk);

    alter table rel_ups_devloc 
        add index FKADC907587504163E (ups_fk), 
        add constraint FKADC907587504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table rel_ups_devname 
        add index FKB5899736CBB6697 (devname_fk), 
        add constraint FKB5899736CBB6697 
        foreign key (devname_fk) 
        references code (pk);

    alter table rel_ups_devname 
        add index FKB5899737504163E (ups_fk), 
        add constraint FKB5899737504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table rel_ups_performer 
        add index FKAA5544618976EC29 (performer_fk), 
        add constraint FKAA5544618976EC29 
        foreign key (performer_fk) 
        references code (pk);

    alter table rel_ups_performer 
        add index FKAA5544617504163E (ups_fk), 
        add constraint FKAA5544617504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table series 
        add index FKCA01FE77B729CB1 (inst_code_fk), 
        add constraint FKCA01FE77B729CB1 
        foreign key (inst_code_fk) 
        references code (pk);

    alter table series 
        add index FKCA01FE774BDB761E (study_fk), 
        add constraint FKCA01FE774BDB761E 
        foreign key (study_fk) 
        references study (pk);

    alter table series 
        add index FKCA01FE778427EAEA (mpps_fk), 
        add constraint FKCA01FE778427EAEA 
        foreign key (mpps_fk) 
        references mpps (pk);

    alter table series_req 
        add index FKE38CD2D6C45E7AAD (accno_issuer_fk), 
        add constraint FKE38CD2D6C45E7AAD 
        foreign key (accno_issuer_fk) 
        references issuer (pk);

    alter table series_req 
        add index FKE38CD2D68151AFEA (series_fk), 
        add constraint FKE38CD2D68151AFEA 
        foreign key (series_fk) 
        references series (pk);

    alter table study 
        add index FK68B0DC9C45E7AAD (accno_issuer_fk), 
        add constraint FK68B0DC9C45E7AAD 
        foreign key (accno_issuer_fk) 
        references issuer (pk);

    alter table study 
        add index FK68B0DC9A511AE1E (patient_fk), 
        add constraint FK68B0DC9A511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    alter table study_on_fs 
        add index FK947DD9374BDB761E (study_fk), 
        add constraint FK947DD9374BDB761E 
        foreign key (study_fk) 
        references study (pk);

    alter table study_on_fs 
        add index FK947DD937206F5C8A (filesystem_fk), 
        add constraint FK947DD937206F5C8A 
        foreign key (filesystem_fk) 
        references filesystem (pk);

    alter table ups 
        add index FK1C538A511AE1E (patient_fk), 
        add constraint FK1C538A511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    alter table ups_rel_ps 
        add index FK9762B0507504163E (ups_fk), 
        add constraint FK9762B0507504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table ups_repl_ps 
        add index FK553160CC7504163E (ups_fk), 
        add constraint FK553160CC7504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table ups_req 
        add index FKF2D951177504163E (ups_fk), 
        add constraint FKF2D951177504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table ups_subscr 
        add index FK99F4BEC97504163E (ups_fk), 
        add constraint FK99F4BEC97504163E 
        foreign key (ups_fk) 
        references ups (pk);

    alter table verify_observer 
        add index FKC9DB73DC37EDB1AA (instance_fk), 
        add constraint FKC9DB73DC37EDB1AA 
        foreign key (instance_fk) 
        references instance (pk);
