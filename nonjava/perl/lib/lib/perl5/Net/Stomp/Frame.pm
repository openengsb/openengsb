package Net::Stomp::Frame;
use strict;
use warnings;
use base 'Class::Accessor::Fast';
__PACKAGE__->mk_accessors(qw(command headers body));

sub as_string {
    my $self    = shift;
    my $command = $self->command;
    my $headers = $self->headers;
    my $body    = $self->body;
    my $frame   = $command . "\n";

    # insert a content-length header
    my $bytes_message = 0;
    if ( $headers->{bytes_message} ) {
        $bytes_message = 1;
        delete $headers->{bytes_message};
        $headers->{"content-length"} = length( $self->body );
    }

    while ( my ( $key, $value ) = each %{ $headers || {} } ) {
        $frame .= $key . ':' . $value . "\n";
    }
    $frame .= "\n";
    $frame .= $body || '';
    $frame .= "\000";
}

sub parse {
    my ( $package, $socket ) = @_;
    local $/ = "\n";

    # read the command
    my $command;
    while (1) {
        $command = $socket->getline || die "Error reading command: $!";
        chop $command;
        last if $command;
    }

    # read headers
    my $headers;
    while (1) {
        my $line = $socket->getline || die "Error reading header: $!";
        chop $line;
        last if $line eq "";
        my ( $key, $value ) = split /: ?/, $line, 2;
        $headers->{$key} = $value;
    }

    # read the body
    my $body;
    if ( $headers->{"content-length"} ) {
        $socket->read( $body, $headers->{"content-length"} )
            || die "Error reading body: $!";
        $socket->getc;    # eat the trailing null
        $headers->{bytes_message} = 1;
    } else {
        while (1) {
            my $byte = $socket->getc;
            die "Error reading body: $!" unless defined $byte;
            last if $byte eq "\000";
            $body .= $byte;
        }
    }

    my $frame = Net::Stomp::Frame->new(
        { command => $command, headers => $headers, body => $body } );
    return $frame;
}

1;

__END__

=head1 NAME

Net::Stomp::Frame - A STOMP Frame

=head1 SYNOPSIS

  use Net::Stomp::Frame;
  my $frame = Net::Stomp::Frame->new( {
    command => $command,
    headers => $headers,
    body    => $body,
  } );
  my $frame  = Net::Stomp::Frame->parse($string);
  my $string = $frame->as_string;
  
=head1 DESCRIPTION

This module encapulates a Stomp frame. Stomp is the Streaming Text
Orientated Messaging Protocol (or the Protocol Briefly Known as TTMP
and Represented by the symbol :ttmp). It's a simple and easy to
implement protocol for working with Message Orientated Middleware from
any language. L<Net::Stomp> is useful for talking to Apache
ActiveMQ, an open source (Apache 2.0 licensed) Java Message Service
1.1 (JMS) message broker packed with many enterprise features.

A Stomp frame consists of a command, a series of headers and a body.

For details on the protocol see L<http://stomp.codehaus.org/Protocol>.

=head1 METHODS

=head2 new

Create a new L<Net::Stomp::Frame> object:

  my $frame = Net::Stomp::Frame->new( {
    command => $command,
    headers => $headers,
    body    => $body,
  } );

=head2 parse

Create a new L<Net::Somp::Frame> given a string containing the serialised frame:

  my $frame  = Net::Stomp::Frame->parse($string);

=head2 as_string

Create a string containing the serialised frame representing the frame:

  my $string = $frame->as_string;

=head1 SEE ALSO

L<Net::Stomp>.

=head1 AUTHOR

Leon Brocard <acme@astray.com>.

=head1 COPYRIGHT

Copyright (C) 2006, Leon Brocard

This module is free software; you can redistribute it or modify it
under the same terms as Perl itself.

