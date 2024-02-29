use std::fs;
use vox_format::{
    data::VoxModels,
    types::{Model, Vector},
    VoxData,
};

use crate::generate::BoundingBox;

pub fn write_model(
    destination: &str,
    name: &str,
    data: &VoxData,
) -> Result<(), vox_format::writer::Error> {
    let path = format!("{destination}/{name}.vox");
    vox_format::to_file(&path, data)
}

pub fn write_label(destination: &str, name: &str, label: &str) -> Result<(), std::io::Error> {
    let path = format!("{destination}/{name}.txt");
    fs::write(&path, label)
}

pub fn wrap_voxels(
    sample_size: Vector<u32>,
    voxels: Vec<vox_format::types::Voxel>,
) -> VoxModels<Model> {
    let mut data = VoxData::default();
    let model = Model {
        size: sample_size,
        voxels,
    };
    data.models.push(model);
    data
}

pub fn create_label(bounding_boxes: &[BoundingBox]) -> String {
    return bounding_boxes
        .iter()
        .map(|bounding_box| {
            format!(
                "{} {} {} {} {} {} {}",
                bounding_box.class,
                bounding_box.from.x,
                bounding_box.from.y,
                bounding_box.from.z,
                bounding_box.to.x,
                bounding_box.to.y,
                bounding_box.to.z
            )
        })
        .collect::<Vec<_>>()
        .join("\n");
}
