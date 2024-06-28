use std::{
    io::{Read, Write},
    net::TcpStream,
};

use jni::{
    objects::{JClass, JString}, sys::jstring, JNIEnv
};

#[no_mangle]
#[allow(clippy::needless_return)]
pub extern "system" fn Java_com_ren_1backend_ren_1backend_NsfwPredictor_predict<'whatever>(
    mut env: JNIEnv,
    _jclass: JClass<'whatever>,
    filename: JString<'whatever>,
) -> jstring {
    log4rs::init_file("log4rs.yaml", Default::default()).ok();

    let filename: String = env
        .get_string(&filename)
        .expect("Couldn't get string")
        .into();

    log::trace!("{:?}", &filename);

    let Ok(host) = std::env::var("NSFW_DAEMON_TCP_HOST") else {
        log::trace!("Provide NSFW_DAEMON_TCP_HOST - a valid host for the daemon to bind");
        return env.new_string("NoHost").expect("Java, you're evil!").into_raw();
    };

    let Ok(port) = std::env::var("NSFW_DAEMON_TCP_PORT") else {
        log::trace!("Provide NSFW_DAEMON_TCP_PORT - a valid port for the daemon to bind");
        return env.new_string("NoPort").expect("Java, you're evil!").into_raw();
    };

    let ip = format!("{}:{}", host, port);

    let Ok(mut listener) = TcpStream::connect(&ip) else {
        log::trace!("Failed to connect to given ip: {}", ip);
        return env.new_string("IPsBad").expect("Java, you're evil!").into_raw();
    };

    let Ok(()) = listener.write_all(filename.as_bytes()) else {
        log::trace!("Failed to connect to given ip: {}", ip);
        return env.new_string("NoWrite").expect("Java, you're evil!").into_raw();
    };

    listener.shutdown(std::net::Shutdown::Write).ok();

    let mut value = [0; 10];
    let Ok(()) = listener.read_exact(&mut value) else {
        log::trace!("Failed to connect to given ip: {}", ip);
        return env.new_string("NoRead").expect("Java, you're evil!").into_raw();
    };

    let Ok(value) = String::from_utf8(value.to_vec()) else {
        return env.new_string("NoUTF8").expect("Java, you're evil!").into_raw();
    };

    return env.new_string(value).expect("Java, you're evil!").into_raw();
}
