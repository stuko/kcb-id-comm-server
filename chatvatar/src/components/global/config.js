module.exports = global.config = {
    ajax: {
        backend: {
            common: {
                url: "http://localhost:8090/api",
            },
            scheduler: {
                url: "http://localhost:8090/scheduler"
            }
            
        }
    }
};