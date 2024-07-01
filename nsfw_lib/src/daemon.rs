use std::{fs::File, io::{Read, Write}, net::TcpListener};

use nsfw::{model::Classification, Model};
use shellexpand::tilde;

fn main() {
    if log4rs::init_file("log4rs.yaml", Default::default()).is_err() {
        eprintln!("Log4rs initialization failed");
    }

    let Some(model) = load_model() else {
        log::error!("Couldn't load model");
        eprintln!("Couldn't load model! Provide model.onnx");
        return;
    };

    let Ok(host) = std::env::var("NSFW_DAEMON_TCP_HOST") else {
        log::error!("Couldn't get NSFW_DAEMON_TCP_HOST");
        eprintln!("Provide NSFW_DAEMON_TCP_HOST - a valid host for the daemon to bind");
        return;
    };

    let Ok(port) = std::env::var("NSFW_DAEMON_TCP_PORT") else {
        log::error!("Couldn't get NSFW_DAEMON_TCP_PORT");
        eprintln!("Provide NSFW_DAEMON_TCP_PORT - a valid port for the daemon to bind");
        return;
    };

    let ip = format!("{}:{}", host, port);

    loop {
        let Ok(listener) = TcpListener::bind(&ip) else {
            log::error!("Couldn't bind to provided NSFW_DAEMON_TCP_PORT");
            eprintln!("Couldn't bind to provided NSFW_DAEMON_TCP_PORT");
            return;
        };

        let Ok((mut stream, _addr)): Result<(std::net::TcpStream, std::net::SocketAddr), std::io::Error> = listener.accept() else {
            log::error!("Couldn't get client.");
            eprintln!("Couldn't get client.");
            return;
        };
        log::trace!("Got client.");

        let mut buf = Vec::with_capacity(128);

        let Ok(_size) = stream.read_to_end(&mut buf) else {
            log::error!("NonUtfChar");
            eprintln!("NonUtfChar");
            write!(stream, "NonUtfChar").ok();
            continue
        };
        log::trace!("Utf chars");

        let Ok(buf) = String::from_utf8(buf) else {
            log::error!("NonUtfChar");
            eprintln!("NonUtfChar");
            write!(stream, "NonUtfChar").ok();
            continue
        };
        log::trace!("Utf chars: {}", buf);

        let Ok(image) = open_image(&buf) else {
            log::error!("NotAnImage: {}", buf);
            eprintln!("NotAnImage: {}", buf);
            write!(stream, "NotAnImage").ok();
            continue
        };
        log::trace!("Image");

        let Ok(results) = nsfw::examine(&model, &image) else {
            write!(stream, "NotClassif").ok();
            continue
        };
        log::trace!("Classif");

        write!(stream, "{:-<10}", estimate(&results)).ok();
    }
}

fn open_image(filename: &str) -> Result<image::RgbaImage, std::io::Error> {
    let filename = tilde(&filename);

    log::trace!("{:?}", &filename);

    let filename = std::fs::canonicalize(std::borrow::Cow::as_ref(&filename))?;

    let image = image::open(filename).map_err(|err| match err {
        image::ImageError::Decoding(_)    => std::io::Error::other(err.to_string()),
        image::ImageError::Encoding(_)    => std::io::Error::other(err.to_string()),
        image::ImageError::Parameter(_)   => std::io::Error::other(err.to_string()),
        image::ImageError::Limits(_)      => std::io::Error::other(err.to_string()),
        image::ImageError::Unsupported(_) => std::io::Error::new(std::io::ErrorKind::NotFound, "Unsupported"),
        image::ImageError::IoError(_)     => std::io::Error::new(std::io::ErrorKind::NotFound, "File is corrupted, but it can be redeemed"),
    })?;

    let image = image.to_rgba8();

    Ok(image)
}

fn load_model() -> Option<Model> {
    let Ok(modelfile) = File::open("model.onnx") else {
        log::error!("Couldn't File::open(\"model.onnx\")");
        eprintln!("Couldn't File::open(\"model.onnx\")");
        return None;
    };

    let Ok(model) = nsfw::create_model(modelfile) else {
        log::error!("Couldn't nsfw::create_model(modelfile)");
        eprintln!("Couldn't nsfw::create_model(modelfile)");
        return None;
    };

    Some(model)
}

#[allow(dead_code)]
fn classif(model: &Model, image: &image::RgbaImage) -> Option<Vec<Classification>> {
    let Ok(classifications) = nsfw::examine(model, image) else {
        log::error!("Couldn't nsfw::examine(model, image)");
        eprintln!("Couldn't nsfw::examine(model, image)");
        return None;
    };

    Some(classifications)
}

fn estimate(classifications: &[Classification]) -> bool {
    let mut normal_sum = 0.0;
    let mut abnormal_sum = 0.0;

    for classif in classifications {
        match &classif.metric {
            nsfw::model::Metric::Neutral => {
                log::trace!("Neutral: {}", classif.score);
                normal_sum += classif.score;
            }
            nsfw::model::Metric::Drawings => {
                log::trace!("Drawings: {}", classif.score);
                normal_sum += classif.score;
            }
            other_metric => {
                log::trace!("{other_metric:?}: {}", classif.score);
                abnormal_sum += classif.score;
            }
        }
    }

    normal_sum >= 10.0 * abnormal_sum
}

#[cfg(test)]
mod tests {
    use std::time::Instant;

    use crate::{classif, estimate, load_model, open_image};

    fn bench<Ret>(fun: impl FnOnce() -> Ret) -> Ret {
        let timer = Instant::now();
        let ret = fun();
        println!(": {:?}", timer.elapsed());
        ret
    }

    #[test]
    fn model_benches() {
        print!("open_image");
        let image = bench(|| open_image("test-images/nsfw_image_2.jpeg")).expect("failed to open image");

        print!("load_model");
        let model = bench(load_model).expect("failed to load model");

        print!("classif");
        let classifications = bench(|| classif(&model, &image)).expect("failed to classify");

        print!("estimate");
        let estimation = bench(|| estimate(&classifications));
        println!("Estimated: {:?}", estimation);
    }
}
