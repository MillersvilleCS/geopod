<?php

// encodes the given text using DES (Digital Encryption Standard) algorithm
// in ECB (Electronic Codebook Mode) mode with PKCS5Padding
function encryptText ($text)
{
	// pad end of text using PKCS5Padding algorithm to be compatible with Java//
	$blockSize = mcrypt_get_block_size ('des', 'ecb');
	$padSize = $blockSize - (strlen ($text) % $blockSize);
	// padSize is needed as pad character to specify # pad bytes
	// Adds extra blockSize when string length is multiple of block size
	//   which is necessary
	$text .= str_repeat (chr ($padSize), $padSize);

	// encode text //
	$encryptedText = mcrypt_encrypt (MCRYPT_DES, "dOp!oEg*", $text, MCRYPT_MODE_ECB);
	$encryptedBase64 = base64_encode ($encryptedText);

	return ($encryptedBase64);
}

function decryptText ($encryptedText)
{
	$base64Text = base64_decode ($encryptedText);
	$text = mcrypt_decrypt (MCRYPT_DES, "dOp!oEg*", $base64Text, MCRYPT_MODE_ECB);
	$len = mb_strlen ($text);
	
	// Compute pad by looking at last character in pad.
	// we arranged for pad = ord (last char in pad).
    	$pad = ord ($text[$len-1]);
	$text = substr ($text, 0, $len-$pad);
	
	return $text;
}

