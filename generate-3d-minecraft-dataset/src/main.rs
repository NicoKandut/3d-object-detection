mod files;
mod generate;

use clap::command;
use clap::Parser;
use files::create_label;
use files::wrap_voxels;
use files::{write_label, write_model};
use generate::{generate_ground, generate_rock, generate_tree};
use rayon::iter::{IntoParallelIterator, ParallelIterator};
use std::fs;
use vox_format::types::Vector;

/// Generate Yolo3D training data
#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    /// Number of training samples to generate
    #[arg(short, long, default_value_t = 1000)]
    train: usize,

    /// Number of validation samples to generate
    #[arg(short, long, default_value_t = 100)]
    val: usize,
}

const SAMPLE_SIZE: i8 = 28;

fn main() {
    let args = Args::parse();
    let sample_size = Vector::new(SAMPLE_SIZE as u32, SAMPLE_SIZE as u32, SAMPLE_SIZE as u32);
    let destination = "C:/src/bac/dataset-3d-minecraft";

    generate_set("train", args.train, sample_size, destination);
    generate_set("val", args.val, sample_size, destination);
}

fn generate_set(set_label: &str, sample_count: usize, sample_size: Vector<u32>, destination: &str) {
    let sample_names = (0..sample_count)
        .into_par_iter()
        .map(|i| {
            let mut voxels = Vec::new();
            let mut rng = rand::thread_rng();

            generate_ground(sample_size, &mut voxels);
            let bounding_boxes = vec![
                generate_tree(&mut rng, &mut voxels),
                generate_tree(&mut rng, &mut voxels),
                generate_rock(&mut rng, &mut voxels),
                generate_rock(&mut rng, &mut voxels),
            ];

            let model = wrap_voxels(sample_size, voxels);
            let name = format!("{set_label}_{i}");
            write_model(&destination, &name, &model).expect("Failed to safe file");
            let label = create_label(&bounding_boxes);
            write_label(&destination, &name, &label).expect("Failed to write label");

            if i % 1000 == 0 {
                println!("Sample {set_label}_{i} created...");
            }

            name
        })
        .collect::<Vec<_>>();

    let path = format!("{destination}/{set_label}.txt");
    let content = sample_names
        .iter()
        .map(|name| format!("{name}.vox {name}.txt"))
        .collect::<Vec<_>>()
        .join("\n");

    fs::write(&path, content).expect("Failed to write index for set");
}
