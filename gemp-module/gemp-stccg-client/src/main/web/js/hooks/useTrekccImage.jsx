import { useEffect, useState } from "react";
import { fetchImage } from '../gemp-022/communication.js';

export function useTrekccImage(url) {
    // store the blob URL outside the useEffect function
    const [blob, setBlob] = useState(null);

    useEffect(() => {
        // setup async func
        async function getImageBlob(the_url) {
            setBlob(null);
            const resultBlob = await fetchImage(the_url);
            setBlob(resultBlob);
        }

        // run async func
        getImageBlob(url);

        // cleanup url if no longer used
        return() => {
            if (blob) {
                URL.revokeObjectURL(blob);
            }
        }
    }, [url]);

    // return whatever the blob is, either null or a blob url
    return blob;
}