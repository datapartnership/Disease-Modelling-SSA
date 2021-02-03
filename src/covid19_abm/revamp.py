import base_model
import params
import scenario_models
import dir_manager
import pickle
import sys
import os


from datetime import datetime, timedelta
timestep=timedelta(hours=4)

#### Set directory
#os.chdir("/Users/swise/workspace/worldbank/Disease-Modelling-SSA/src/covid19_abm")
os.chdir("/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/src/covid19_abm")
cwd = os.getcwd()

stay_duration_file = dir_manager.get_data_dir('preprocessed', 'mobility', 'New Files', 'weekday_mobility_duration_count_df-new-district i5.csv')
transition_probability_file = dir_manager.get_data_dir('preprocessed', 'mobility', 'New Files', 'daily_region_transition_probability-new-district-pre-lockdown_i5.csv')
reduced_transition_probability_file = dir_manager.get_data_dir('preprocessed', 'mobility', 'New Files', 'daily_region_transition_probability-new-district-post-lockdown_i5.csv')



sample_size = 10
R0 = 1.3

params = params.ParamsConfig(
    district='new', data_sample_size=sample_size, R0=R0,
    normal_interaction_matrix_file=('../../configs/interaction_matrix_nld.txt'), 
    lockdown_interaction_matrix_file=('../../configs/interaction_matrix_ld.txt'), 
    stay_duration_file=dir_manager.get_data_dir('preprocessed', 'mobility', stay_duration_file),
    transition_probability_file=dir_manager.get_data_dir('preprocessed', 'mobility', transition_probability_file),
    #intra_district_decreased_mobility_rates_file=dir_manager.get_data_dir('preprocessed', 'mobility', transition_probability_file),
    timestep=timestep)

params.set_new_district_seed(seed_infected=2)

#model = scenario_models.Phase1GovernmentOpenSchoolsScenario(params)
model = scenario_models.DynamicPhase1GovernmentOpenSchoolsScenario(params)

params.data_file_name = '../../data/preprocessed/census/zimbabwe_expanded_census_consolidated_100pct.pickle'
model.load_agents(params.data_file_name, size=None, infect_num=params.SEED_INFECT_NUM)
end_date = datetime(2021, 6, 1)

# just to make sure!
for i in range(100):
	model.step()

print(model.scheduler.real_time)

model_dump_file = dir_manager.get_data_dir('logs', f'model_dump_file_blah.pickle')
with open(model_dump_file, 'wb') as fl:
	pickle.dump(model, fl)
