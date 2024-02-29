use rand::Rng;
use vox_format::types::{Vector, Voxel};

use crate::SAMPLE_SIZE;

const PALETTE_INDEX_GRASS: u8 = 190;
const PALETTE_INDEX_WOOD: u8 = 137;
const PALETTE_INDEX_LEAVES: u8 = 204;
const PALETTE_INDEX_ROCK: u8 = 251;

pub struct BoundingBox {
    pub class: i8,
    pub from: Vector<i8>,
    pub to: Vector<i8>,
}

pub fn generate_tree(rng: &mut rand::prelude::ThreadRng, voxels: &mut Vec<Voxel>) -> BoundingBox {
    // Generate Tree
    let x_start: i8 = rng.gen_range(0..(SAMPLE_SIZE - 6));
    let y_start: i8 = rng.gen_range(0..(SAMPLE_SIZE - 6));
    let z_start: i8 = 1;

    let x_end = x_start + 5;
    let y_end = y_start + 5;
    let z_end = z_start + rng.gen_range(5..9);

    for z in z_start..(z_end - 4) {
        let voxel = Voxel::new(Vector::new(x_start + 2, y_start + 2, z), PALETTE_INDEX_WOOD);
        voxels.push(voxel);
    }

    for z in (z_end - 4)..(z_end - 2) {
        for y in (y_start)..(y_end) {
            for x in (x_start)..(x_end) {
                let palette_index = if x == x_start + 2 && y == y_start + 2 {
                    PALETTE_INDEX_WOOD
                } else {
                    PALETTE_INDEX_LEAVES
                };

                let voxel = Voxel::new(Vector::new(x, y, z), palette_index);
                voxels.push(voxel);
            }
        }
    }

    for z in (z_end - 2)..z_end {
        for y in (y_start + 1)..(y_end - 1) {
            for x in (x_start + 1)..(x_end - 1) {
                let voxel = Voxel::new(Vector::new(x, y, z), PALETTE_INDEX_LEAVES);
                voxels.push(voxel);
            }
        }
    }

    BoundingBox {
        class: 0,
        from: Vector::new(x_start, y_start, z_start),
        to: Vector::new(x_end, y_end, z_end),
    }
}

pub fn generate_rock(rng: &mut rand::prelude::ThreadRng, voxels: &mut Vec<Voxel>) -> BoundingBox {
    // Generate Tree
    let x_start: i8 = rng.gen_range(0..(SAMPLE_SIZE - 6));
    let y_start: i8 = rng.gen_range(0..(SAMPLE_SIZE - 6));
    let z_start: i8 = 1;

    let x_end = x_start + 5;
    let y_end = y_start + 5;
    let z_end = z_start + rng.gen_range(5..9);

    for z in z_start..z_end {
        for y in y_start..y_end {
            for x in x_start..x_end {
                let voxel = Voxel::new(Vector::new(x, y, z), PALETTE_INDEX_ROCK);
                voxels.push(voxel);
            }
        }
    }

    BoundingBox {
        class: 1,
        from: Vector::new(x_start, y_start, z_start),
        to: Vector::new(x_end, y_end, z_end),
    }
}

pub fn generate_ground(sample_size: Vector<u32>, voxels: &mut Vec<Voxel>) {
    // Generate ground
    for y in 0i8..sample_size.y as i8 {
        for x in 0i8..sample_size.x as i8 {
            let voxel = Voxel::new(Vector::new(x, y, 0i8), PALETTE_INDEX_GRASS);
            voxels.push(voxel);
        }
    }
}