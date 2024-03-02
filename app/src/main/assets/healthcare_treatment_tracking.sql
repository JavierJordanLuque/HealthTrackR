CREATE TABLE IF NOT EXISTS `USER` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `email` TEXT NOT NULL,
  `password` BLOB NULL,
  `salt` BLOB NULL,
  `full_name` TEXT NOT NULL,
  `birth_date` DATE NULL,
  `gender` TEXT NULL,
  `blood_type` BLOB NULL,
  `blood_type_iv` BLOB NULL
);

CREATE TABLE IF NOT EXISTS `TREATMENT` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `user_id` INTEGER NOT NULL,
  `title` BLOB NOT NULL,
  `title_iv` BLOB NOT NULL,
  `start_date` BLOB NOT NULL,
  `start_date_iv` BLOB NOT NULL,
  `end_date` BLOB NULL,
  `end_date_iv` BLOB NULL,
  `diagnosis` BLOB NULL,
  `diagnosis_iv` BLOB NULL,
  `category` BLOB NULL,
  `category_iv` BLOB NULL,
  FOREIGN KEY (`user_id`) REFERENCES `USER` (`id`)
);

CREATE TABLE IF NOT EXISTS `MEDICINE` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` BLOB NOT NULL,
  `name_iv` BLOB NOT NULL,
  `active_substance` BLOB NULL,
  `active_substance_iv` BLOB NULL
);

CREATE TABLE IF NOT EXISTS `TREATMENT_MEDICINE` (
  `treatment_id` INTEGER NOT NULL,
  `medicine_id` INTEGER NOT NULL,
  `dose` INTEGER NULL,
  `administration_route` TEXT NULL,
  `initial_dosing_time` DATETIME NOT NULL,
  `dosage_frequency_hours` INTEGER NOT NULL,
  `dosage_frequency_minutes` INTEGER NOT NULL,
  PRIMARY KEY (treatment_id, medicine_id),
  FOREIGN KEY (`treatment_id`) REFERENCES `TREATMENT` (`id`),
  FOREIGN KEY (`medicine_id`) REFERENCES `MEDICINE` (`id`)
);

CREATE TABLE IF NOT EXISTS `MEDICAL_APPOINTMENT` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `treatment_id` INTEGER NOT NULL,
  `purpose` TEXT NULL,
  `date` BLOB NOT NULL,
  `date_iv` BLOB NOT NULL,
  `time` TEXT NOT NULL,
  `latitude` REAL NULL,
  `longitude` REAL NULL,
  `pending` INTEGER NOT NULL,
  FOREIGN KEY (`treatment_id`) REFERENCES `TREATMENT` (`id`)
);

CREATE TABLE IF NOT EXISTS `NOTIFICATION` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `treatment_id` INTEGER NULL,
  `medicine_id` INTEGER NULL,
  `medical_appointment_id` INTEGER NULL,
  `timestamp` INTEGER NOT NULL,
  FOREIGN KEY (`treatment_id`) REFERENCES `TREATMENT` (`id`),
  FOREIGN KEY (`medicine_id`) REFERENCES `MEDICINE` (`id`),
  FOREIGN KEY (`medical_appointment_id`) REFERENCES `MEDICAL_APPOINTMENT` (`id`)
);

CREATE TABLE IF NOT EXISTS `ALLERGY` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `user_id` INTEGER NOT NULL,
  `name` BLOB NOT NULL,
  `name_iv` BLOB NOT NULL,
  FOREIGN KEY (`user_id`) REFERENCES `USER` (`id`)
);

CREATE TABLE IF NOT EXISTS `PREVIOUS_MEDICAL_CONDITION` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `user_id` INTEGER NOT NULL,
  `name` BLOB NOT NULL,
  `name_iv` BLOB NOT NULL,
  FOREIGN KEY (`user_id`) REFERENCES `USER` (`id`)
);

CREATE TABLE IF NOT EXISTS `STEP` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `treatment_id` INTEGER NOT NULL,
  `title` TEXT NOT NULL,
  `description` TEXT NULL,
  `num_order` INTEGER NOT NULL,
  FOREIGN KEY (`treatment_id`) REFERENCES `TREATMENT` (`id`)
);

CREATE TABLE IF NOT EXISTS `MULTIMEDIA` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `step_id` INTEGER NOT NULL,
  `type` TEXT NOT NULL,
  `path` TEXT NOT NULL,
  FOREIGN KEY (`step_id`) REFERENCES `STEP` (`id`)
);

CREATE TABLE IF NOT EXISTS `SYMPTOM` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `treatment_id` INTEGER NOT NULL,
  `description` BLOB NOT NULL,
  `description_iv` BLOB NOT NULL,
  FOREIGN KEY (`treatment_id`) REFERENCES `TREATMENT` (`id`)
);

CREATE TABLE IF NOT EXISTS `QUESTION` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `treatment_id` INTEGER NOT NULL,
  `description` TEXT NOT NULL,
  FOREIGN KEY (`treatment_id`) REFERENCES `TREATMENT` (`id`)
);