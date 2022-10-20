/* ******************************************

Program name: ABM scale up
Author: SA
Date last modified: 
Project:  ABM
Purpose:   scale back up from original 5%

****************************************** */

***0. SET STATA 
clear matrix
clear
set more off

************ recreate the subsamples by taking the original IPUMS 5% and then scale back UP to 20% and 50% for the paper. Creating different versions:

/*

1. No age, all households of 6 (synthetic) (variation -1)
2. With real age and households (variation 1)
3. With real age, household, multi district (variation 3)

Multiply each by 4  to create 20% 
(consider 50% later)

*/


** Keep only the core variables from IPUMS That is: 	person_id	age	sex	household_id	district_id	economic_status	economic_activity_location_id	school_goers	manufacturing_workers

************ recreate the dummy subsamples and the same 5% and 20% and 50% and 75% and 100%
clear all

cap cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA"
cap cd "\Users\wb488473\OneDrive - WBG\Documents\GitHub\Disease-Modelling-SSA"
cap cd "/Users/sophieayling/Library/CloudStorage/OneDrive-UniversityCollegeLondon/GitHub/Disease-Modelling-SSA/"
tempfile temp1 temp2


** take the 100 perc dataset to sample on

use "data/raw/census/100_perc/abm_individual_new_092320_final_merged_complete_FINAL.dta", clear

* it still needs some of the work done down below, but first cut it to the sample size i want - using new method from Billy using cycle 


keep age sex serial new_district_id economic_status school_goers manufacturing_workers cycle  serial_cycle_pernum


rename serial household_id
rename serial_cycle_pernum person_id
rename new_district_id district_id

codebook person_id   // 
codebook household_id // 

// keep person_id	age sex	household_id district_id economic_status school_goers manufacturing_workers --  economic activity location id

*****************************************create variation 3************************************


/* characteristics:
-  multi district
-  one econ status
-  real ages
-  real hh sizes
*/

*make everyone have the same econ status
decode economic_status, gen(ec_st)
tab ec_st, nol
replace ec_st = "Default"
drop economic_status 
rename ec_st economic_status

// multidist, ages, hh size stay the same 

**save the 100 perc 

save "data/raw/census/100_perc_sample/abm_092320_100_perc_080222_ver3.dta", replace



*** CHECKS BEFORE
//
// tabout age using "data/raw/census/checks/age_100p_v3.xls", c(col freq) replace
// tabout sex using "data/raw/census/checks/sex_100p_v3.xls", c(col freq) replace
// tabout school_goers using "data/raw/census/checks/school_goers_100p_v3.xls", c(col freq)  replace
// tabout economic_status using "data/raw/census/checks/econ_status_100p_v3.xls", c(col freq) replace
// tabout manufacturing_workers using "data/raw/census/checks/manu_workers_100p_v3.xls", c(col freq) replace
// tabout district_id using "data/raw/census/checks/district_id_100p_v3.xls", c(col freq)  replace


*********************************************** CREATE 5%

destring cycle, replace
keep if cycle == 1
tab cycle

*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 654, 688
codebook household_id // 160,728 hhs


save "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver3.dta", replace

*** CHECKS AFTER
tabout age using "data/raw/census/checks/age_5p_v3.xls", c(col freq) replace
tabout sex using "data/raw/census/checks/sex_5p_v3.xls", c(col freq)  replace
tabout school_goers using "data/raw/census/checks/school_goers_5p_v3.xls", c(col freq) replace
tabout economic_status using "data/raw/census/checks/econ_status_5p_v3.xls", c(col freq) replace
tabout manufacturing_workers using "data/raw/census/checks/manu_workers_5p_v3.xls", c(col freq) replace
tabout district_id using "data/raw/census/checks/district_id_5p_v3.xls", c(col freq) replace

*********************************************** CREATE 10%

expand 2

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

*replacing all person ids and hh ids to correspond to 10%

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
//gen num="num"
egen new_id=concat(household_id cycle)
drop household_id // num 
rename new_id household_id

*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 1.3 million
codebook household_id // 321,456 hhs
e
save "data/raw/census/10_perc_sample/abm_092320_10_perc_080222_ver3.dta", replace

*********************************************** CREATE 25%

use "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver3.dta", clear


expand 5

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

*replacing all person ids and hh ids to correspond to 25%

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
gen num="num"
egen new_id=concat(household_id cycle)
drop household_id num 
rename new_id household_id

*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 3.2 million
codebook household_id // 803,640 hhs

save "data/raw/census/25_perc_sample/abm_092320_25_perc_080222_ver3.dta", replace


*********************************************** CREATE 50 from 5%

use "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver3.dta", clear

expand 10

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
gen num="num"
egen new_id=concat(household_id cycle)
drop household_id num 
rename new_id household_id


*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 6.5 million
codebook household_id  // 1.6 million

save "data/raw/census/50_perc_sample/abm_092320_50_perc_080222_ver3.dta", replace


*********************************************** CREATE 75 from 5%


use "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver3.dta", clear

expand 15

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
gen num="num"
egen new_id=concat(household_id  num cycle)
drop household_id num
rename new_id household_id


*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 9.8 million
codebook household_id // 2.4 million


save "data/raw/census/75_perc_sample/abm_092320_75_perc_080222_ver3.dta", replace
